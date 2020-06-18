package org.dockbox.corona.core.packets;

import org.dockbox.corona.core.util.CommonUtil;

import java.sql.Time;

public class SendAlertPacket extends Packet {

    public static final SendAlertPacket EMPTY = new SendAlertPacket(null, null, null);

    private final String id;
    private final Time alerted;
    private final Time contactInfected;

    public SendAlertPacket(String id, Time alerted, Time contactInfected) {
        this.id = id;
        this.alerted = alerted;
        this.contactInfected = contactInfected;
    }

    public String getId() {
        return id;
    }

    public Time getAlerted() {
        return alerted;
    }

    public Time getContactInfected() {
        return contactInfected;
    }


    @Override
    public String serialize() {
        return new StringBuilder()
                .append("ID=").append(this.getId())
                .append("\nTIMESTAMP_ALERTED=").append(CommonUtil.parseTimeString(this.getAlerted()))
                .append("\nCONTACT_TIMESTAMP=").append(CommonUtil.parseTimeString(this.getContactInfected()))
                .toString();
    }

    @Override
    public Packet deserialize(String message) {
        String[] lines = message.split("\n");
        SendAlertPacket.Builder builder = new SendAlertPacket.Builder();
        for (String line : lines) {
            String[] keyValue = line.split("=");
            String key = keyValue[0];
            String value = keyValue[1];

            switch (key) {
                case "ID":
                    builder.withId(value);
                    break;
                case "TIMESTAMP_ALERTED":
                    builder.withSent(CommonUtil.parseTime(value));
                    break;
                case "CONTACT_TIMESTAMP":
                    builder.withContactInfected(CommonUtil.parseTime(value));
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect packet format");
            }
        }

        return builder.build();
    }

    private static final class Builder {
        private String id;
        private Time alerted;
        private Time contactInfected;

        public SendAlertPacket.Builder withId(String id) {
            this.id = id;
            return this;
        }

        public SendAlertPacket.Builder withSent(Time alerted) {
            this.alerted = alerted;
            return this;
        }

        public SendAlertPacket.Builder withContactInfected(Time contactReceived) {
            this.contactInfected = contactReceived;
            return this;
        }

        public SendAlertPacket build() {
            return new SendAlertPacket(id, alerted, contactInfected);
        }
    }
}
