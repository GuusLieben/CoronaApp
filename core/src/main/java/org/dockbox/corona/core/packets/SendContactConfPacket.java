package org.dockbox.corona.core.packets;

import org.dockbox.corona.core.util.CommonUtil;

import java.util.Date;

public class SendContactConfPacket extends Packet {

    public static final SendContactConfPacket EMPTY = new SendContactConfPacket(null, null, null, null);

    private final String id;
    private final String contactId;
    private final Date contactSent;
    private final Date contactReceived;

    public SendContactConfPacket(String id, String contactId, Date contactSent, Date contactReceived) {
        this.id = id;
        this.contactId = contactId;
        this.contactSent = contactSent;
        this.contactReceived = contactReceived;
    }

    public String getId() {
        return id;
    }

    public String getContactId() {
        return contactId;
    }

    public Date getContactSent() {
        return contactSent;
    }

    public Date getContactReceived() {
        return contactReceived;
    }

    @Override
    public String getHeader() {
        return "SEND::CONTACT_CONF";
    }

    @Override
    public String serialize() {
        return new StringBuilder()
                .append("ID=").append(this.getId())
                .append("\nCONTACT_ID=").append(this.getContactId())
                .append("\nTIMESTAMP_CONTACT_SENT=").append(CommonUtil.parseDateString(this.getContactSent()))
                .append("\nTIMESTAMP_CONTACT_CONFIRMED=").append(CommonUtil.parseDateString(this.getContactReceived()))
                .toString();
    }

    @Override
    public SendContactConfPacket deserialize(String message) {
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
                case "CONTACT_ID":
                    builder.withContactId(value);
                    break;
                case "TIMESTAMP_CONTACT_SENT":
                    builder.withContactSent(CommonUtil.parseDate(value));
                    break;
                case "TIMESTAMP_CONTACT_CONFIRMED":
                    builder.withContactReceived(CommonUtil.parseDate(value));
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect packet format");
            }
        }

        return builder.build();
    }

    private static final class Builder {
        private String id;
        private String contactId;
        private Date contactSent;
        private Date contactReceived;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withContactId(String contactId) {
            this.contactId = contactId;
            return this;
        }

        public Builder withContactSent(Date contactSent) {
            this.contactSent = contactSent;
            return this;
        }

        public Builder withContactReceived(Date contactReceived) {
            this.contactReceived = contactReceived;
            return this;
        }

        public SendContactConfPacket build() {
            return new SendContactConfPacket(id, contactId, contactSent, contactReceived);
        }
    }
}
