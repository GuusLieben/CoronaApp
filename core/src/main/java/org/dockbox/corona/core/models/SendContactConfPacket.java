package org.dockbox.corona.core.models;

import java.sql.Time;

public class SendContactConfPacket extends Packet {

    private final String id;
    private final String contactId;
    private final Time contactSent;
    private final Time contactReceived;

    public SendContactConfPacket(String id, String contactId, Time contactSent, Time contactReceived) {
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

    public Time getContactSent() {
        return contactSent;
    }

    public Time getContactReceived() {
        return contactReceived;
    }

    public static final class SendContactConfPacketBuilder {
        private String id;
        private String contactId;
        private Time contactSent;
        private Time contactReceived;

        public SendContactConfPacketBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public SendContactConfPacketBuilder withContactId(String contactId) {
            this.contactId = contactId;
            return this;
        }

        public SendContactConfPacketBuilder withContactSent(Time contactSent) {
            this.contactSent = contactSent;
            return this;
        }

        public SendContactConfPacketBuilder withContactReceived(Time contactReceived) {
            this.contactReceived = contactReceived;
            return this;
        }

        public SendContactConfPacket build() {
            return new SendContactConfPacket(id, contactId, contactSent, contactReceived);
        }
    }
}
