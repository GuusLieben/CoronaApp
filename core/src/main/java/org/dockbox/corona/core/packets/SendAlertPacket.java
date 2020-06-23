package org.dockbox.corona.core.packets;

import org.dockbox.corona.core.util.Util;

import java.util.Date;

public class SendAlertPacket extends Packet {

    public static final SendAlertPacket EMPTY = new SendAlertPacket(null, null, null);

    private final String id;
    private final Date alerted;
    private final Date contactInfected;

    public SendAlertPacket(String id, Date alerted, Date contactInfected) {
        this.id = id;
        this.alerted = alerted;
        this.contactInfected = contactInfected;
    }

    public String getId() {
        return id;
    }

    public Date getAlerted() {
        return alerted;
    }

    public Date getContactInfected() {
        return contactInfected;
    }


    @Override
    public String getHeader() {
        return "SEND::ALERT";
    }

    @Override
    public String serialize() {
        return new StringBuilder()
                .append("ID=").append(this.getId())
                .append("\nTIMESTAMP_ALERTED=").append(Util.parseDateString(this.getAlerted()))
                .append("\nCONTACT_TIMESTAMP=").append(Util.parseDateString(this.getContactInfected()))
                .toString();
    }

    @Override
    public SendAlertPacket deserialize(String message) {
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
                    builder.withSent(Util.parseDate(value));
                    break;
                case "CONTACT_TIMESTAMP":
                    builder.withContactInfected(Util.parseDate(value));
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect packet format");
            }
        }

        return builder.build();
    }

    private static final class Builder {
        private String id;
        private Date alerted;
        private Date contactInfected;

        public SendAlertPacket.Builder withId(String id) {
            this.id = id;
            return this;
        }

        public SendAlertPacket.Builder withSent(Date alerted) {
            this.alerted = alerted;
            return this;
        }

        public SendAlertPacket.Builder withContactInfected(Date contactReceived) {
            this.contactInfected = contactReceived;
            return this;
        }

        public SendAlertPacket build() {
            return new SendAlertPacket(id, alerted, contactInfected);
        }
    }
}
