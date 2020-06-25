package org.dockbox.corona.core.packets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SendContactsPacket extends Packet {

    public static final SendContactsPacket EMPTY = new SendContactsPacket(null);

    public final List<String> contacts;

    public SendContactsPacket(List<String> contacts) {
        this.contacts = contacts;
    }

    public List<String> getContacts() {
        return contacts;
    }

    @Override
    public String getHeader() {
        return "SEND::CONTACTS";
    }


    @Override
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        String contactStr = String.join(",", contacts);
        sb.append("CONTACTS=").append(contactStr);
        return sb.toString();
    }

    @Override
    public SendContactsPacket deserialize(String message) {
        String[] lines = message.split("\n");
        SendContactsPacket.Builder builder = new SendContactsPacket.Builder();
        for (String line : lines) {
            String[] keyValue = line.split("=");
            String key = keyValue[0];
            String value = keyValue[1];
            if ("CONTACTS".equals(key)) {
                String[] contactStr = value.split(",");
                builder.withContacts(new ArrayList<>(Arrays.asList(contactStr)));
            } else {
                throw new IllegalArgumentException("Incorrect packet format");
            }
        }
        return builder.build();
    }


    public static final class Builder {
        public List<String> contacts;

        private Builder() {
        }

        public Builder withContacts(List<String> contacts) {
            this.contacts = contacts;
            return this;
        }

        public SendContactsPacket build() {
            return new SendContactsPacket(contacts);
        }
    }
}
