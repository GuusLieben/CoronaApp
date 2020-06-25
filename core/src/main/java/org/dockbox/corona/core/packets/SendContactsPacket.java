package org.dockbox.corona.core.packets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SendContactsPacket extends Packet {

    public static final SendContactsPacket EMPTY = new SendContactsPacket(null, null);

    private final String userId;
    private final List<String> contacts;

    public SendContactsPacket(String userId, List<String> contacts) {
        this.userId = userId;
        this.contacts = contacts;
    }

    public String getUserId() {
        return userId;
    }

    public List<String> getContacts() {
        return contacts.stream().filter(line -> !"".equals(line)).collect(Collectors.toList());
    }

    @Override
    public String getHeader() {
        return "SEND::CONTACTS";
    }


    @Override
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        String contactStr = String.join(",", contacts);
        sb.append("CONTACTS=").append(contactStr)
                .append("\nID=").append(userId);
        return sb.toString();
    }

    @Override
    public SendContactsPacket deserialize(String message) {
        String[] lines = message.split("\n");
        SendContactsPacket.Builder builder = new SendContactsPacket.Builder();
        for (String line : lines) {
            if (!line.contains("=")) continue;
            String[] keyValue = line.split("=");
            String key = keyValue[0];
            String value = keyValue.length > 1 ? keyValue[1] : "";
            if ("CONTACTS".equals(key)) {
                String[] contactStr = value.split(",");
                builder.withContacts(new ArrayList<>(Arrays.asList(contactStr)));
            } else if ("ID".equals(key)) {
                builder.withUserId(value);
            } else {
                throw new IllegalArgumentException("Incorrect packet format");
            }
        }
        return builder.build();
    }


    public static final class Builder {
        public List<String> contacts;
        public String userId;

        private Builder() {
        }

        public Builder withContacts(List<String> contacts) {
            this.contacts = contacts;
            return this;
        }

        public Builder withUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public SendContactsPacket build() {
            return new SendContactsPacket(userId, contacts);
        }
    }
}
