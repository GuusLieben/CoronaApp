package org.dockbox.corona.core.packets;

import org.dockbox.corona.core.util.CommonUtil;

import java.sql.Time;

public class SendAlertPacket extends Packet {

    public static final SendAlertPacket EMPTY = new SendAlertPacket(null, null, null);

    private final String id;
    private final Time sent;
    private final Time contactInfected;

    public SendAlertPacket(String id, Time sent, Time contactInfected) {
        this.id = id;
        this.sent = sent;
        this.contactInfected = contactInfected;
    }

    public String getId() {
        return id;
    }

    public Time getSent() {
        return sent;
    }

    public Time getContactInfected() {
        return contactInfected;
    }


    @Override
    public String serialize() {
        return new StringBuilder()
                .append("ID=").append(this.getId())
                .append("\nTIMESTAMP=").append(CommonUtil.parseTimeString(this.getSent()))
                .append("\nTIMESTAMP_CONTACT=").append(CommonUtil.parseTimeString(this.getContactInfected()))
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
                case "TIMESTAMP":
                    builder.withSent(CommonUtil.parseTime(value));
                    break;
                case "TIMESTAMP_CONTACT":
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
        private Time sent;
        private Time contactInfected;

        public SendAlertPacket.Builder withId(String id) {
            this.id = id;
            return this;
        }

        public SendAlertPacket.Builder withSent(Time contactSent) {
            this.sent = contactSent;
            return this;
        }

        public SendAlertPacket.Builder withContactInfected(Time contactReceived) {
            this.contactInfected = contactReceived;
            return this;
        }

        public SendAlertPacket build() {
            return new SendAlertPacket(id, sent, contactInfected);
        }
    }
}
