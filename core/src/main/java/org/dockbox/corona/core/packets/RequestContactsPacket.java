package org.dockbox.corona.core.packets;

import org.jetbrains.annotations.Nullable;

public class RequestContactsPacket extends Packet {
    public final String userId;

    public RequestContactsPacket(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public String getHeader() {
        return "REQUEST::CONTACTS";
    }

    @Override
    public String serialize() {
        return new StringBuilder()
                .append("ID=").append(userId)
                .toString();
    }

    @Override
    public Packet deserialize(@Nullable String message) {
        String[] lines = message.split("\n");
        RequestContactsPacket.Builder builder = new RequestContactsPacket.Builder();
        for (String line : lines) {
            String[] keyValue = line.split("=");
            String key = keyValue[0];
            String value = keyValue[1];

            if ("ID".equals(key)) {
                builder.withUserId(value);
            } else {
                throw new IllegalArgumentException("Incorrect packet format");
            }
        }
        return builder.build();
    }


    public static final class Builder {
        public String userId;

        private Builder() {
        }

        public Builder withUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public RequestContactsPacket build() {
            return new RequestContactsPacket(userId);
        }
    }
}
