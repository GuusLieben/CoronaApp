package org.dockbox.corona.core.packet;

import org.dockbox.corona.core.models.SendContactConfPacket;
import org.dockbox.corona.core.util.CommonUtil;

public class SendContactConfPacketParser extends PacketParser<SendContactConfPacket> {

    @Override
    public String serialize(SendContactConfPacket model) {
        return new StringBuilder()
                .append("ID=").append(model.getId())
                .append("\nCONTACT_ID=").append(model.getContactId())
                .append("\nTIMESTAMP_CONTACT_SENT=").append(CommonUtil.parseTimeString(model.getContactSent()))
                .append("\nTIMESTAMP_CONTACT_CONFIRMED=").append(CommonUtil.parseTimeString(model.getContactReceived()))
                .toString();
    }

    @Override
    public SendContactConfPacket deserialize(String message) {
        String[] lines = message.split("\n");
        SendContactConfPacket.SendContactConfPacketBuilder builder = new SendContactConfPacket.SendContactConfPacketBuilder();
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
                    builder.withContactSent(CommonUtil.parseTime(value));
                    break;
                case "TIMESTAMP_CONTACT_CONFIRMED":
                    builder.withContactReceived(CommonUtil.parseTime(value));
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect packet format");
            }
        }

        return builder.build();
    }

}
