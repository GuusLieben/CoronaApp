package org.dockbox.corona.core.packets;

import org.dockbox.corona.core.util.CommonUtil;

import java.sql.Time;

public class SendInfectConfPacket extends Packet {

    public final static SendInfectConfPacket EMPTY = new SendInfectConfPacket(null, null);

    private final String id;
    private final Time infected;

    public SendInfectConfPacket(String id, Time infected) {
        this.id = id;
        this.infected = infected;
    }

    public String getId() {
        return id;
    }

    public Time getInfected() {
        return infected;
    }

    @Override
    public String serialize() {
        return new StringBuilder()
                .append("ID=").append(this.getId())
                .append("\nTIMESTAMP_INFECTED=").append(CommonUtil.parseTimeString(this.getInfected()))
                .toString();
    }

    @Override
    public SendInfectConfPacket deserialize(String message) {
        String[] lines = message.split("\n");
        SendInfectConfPacket.Builder builder = new SendInfectConfPacket.Builder();
        for (String line : lines) {
            String[] keyValue = line.split("=");
            String key = keyValue[0];
            String value = keyValue[1];

            switch (key) {
                case "ID":
                    builder.withId(value);
                    break;
                case "TIMESTAMP_INFECTED":
                    builder.withContactSent(CommonUtil.parseTime(value));
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect packet format");
            }
        }

        return builder.build();
    }

    public static final class Builder {
        private String id;
        private Time infected;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withContactSent(Time infected) {
            this.infected = infected;
            return this;
        }

        public SendInfectConfPacket build() {
            return new SendInfectConfPacket(id, infected);
        }
    }
}