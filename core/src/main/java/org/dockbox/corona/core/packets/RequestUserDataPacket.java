package org.dockbox.corona.core.packets;

import org.dockbox.corona.core.util.Util;

import java.util.Date;

public class RequestUserDataPacket extends Packet {

    public static final RequestUserDataPacket EMPTY = new RequestUserDataPacket(null, null);

    private final String id;
    private final Date timestamp;

    public RequestUserDataPacket(String id, Date timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    private RequestUserDataPacket(Builder builder) {
        id = builder.id;
        timestamp = builder.timestamp;
    }

    public String getId() {
        return id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public String getHeader() {
        return "REQUEST::USER_DATA";
    }

    @Override
    public String serialize() {
        return new StringBuilder()
                .append("ID=").append(id)
                .append("\nTIMESTAMP_REQUEST=").append(Util.parseDateString(timestamp))
                .toString();
    }

    @Override
    public Packet deserialize(String message) {
        String[] lines = message.split("\n");
        Builder builder = new Builder();
        for (String line : lines) {
            String[] keyValue = line.split("=");
            String key = keyValue[0];
            String value = keyValue[1];

            switch (key) {
                case "ID":
                    builder.withId(value);
                    break;
                case "TIMESTAMP_REQUEST":
                    builder.withTimestamp(Util.parseDate(value));
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect packet format");
            }
        }
        return builder.build();
    }

    public static final class Builder {
        private String id;
        private Date timestamp;

        public Builder() {
        }

        public Builder withId(String val) {
            id = val;
            return this;
        }

        public Builder withTimestamp(Date val) {
            timestamp = val;
            return this;
        }

        public RequestUserDataPacket build() {
            return new RequestUserDataPacket(this);
        }
    }
}
