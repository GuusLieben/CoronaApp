package org.dockbox.corona.core.packets;

import org.dockbox.corona.core.util.CommonUtil;

import java.sql.Time;

public class RequestUserDataPacket extends Packet {

    public static final RequestUserDataPacket EMPTY = new RequestUserDataPacket(null, null);

    private final String id;
    private final Time timestamp;

    public RequestUserDataPacket(String id, Time timestamp) {
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

    public Time getTimestamp() {
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
                .append("\nTIMESTAMP_REQUEST=").append(CommonUtil.parseTimeString(timestamp))
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
                    builder.withTimestamp(CommonUtil.parseTime(value));
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect packet format");
            }
        }
        return builder.build();
    }

    public static final class Builder {
        private String id;
        private Time timestamp;

        public Builder() {
        }

        public Builder withId(String val) {
            id = val;
            return this;
        }

        public Builder withTimestamp(Time val) {
            timestamp = val;
            return this;
        }

        public RequestUserDataPacket build() {
            return new RequestUserDataPacket(this);
        }
    }
}
