package org.dockbox.corona.core.packets;

import org.dockbox.corona.core.model.User;
import org.dockbox.corona.core.util.Util;

import java.util.Date;

public class SendUserDataPacket extends Packet {

    public static final SendUserDataPacket EMPTY = new SendUserDataPacket(null, null);

    private final User user;
    private final Date received;

    public SendUserDataPacket(User user, Date received) {
        this.user = user;
        this.received = received;
    }

    private SendUserDataPacket(Builder builder) {
        user = builder.user;
        received = builder.received;
    }

    @Override
    public String getHeader() {
        return "SEND::USER_DATA";
    }

    @Override
    public String serialize() {
        return new StringBuilder()
                .append("ID=").append(user.getId())
                .append("\nFIRSTNAME=").append(user.getFirstName())
                .append("\nLASTNAME=").append(user.getLastName())
                .append("\nBSN=").append(user.getBSN())
                .append("\nBIRTHDATE=").append(Util.parseDateString(user.getBirthDate()))
                .append("\nTIMESTAMP_RECEIVED=").append(Util.parseDateString(received))
                .toString();
    }

    @Override
    public Packet deserialize(String message) {
        String[] lines = message.split("\n");
        Builder builder = new Builder();
        User.Builder userBuilder = new User.Builder();
        for (String line : lines) {
            String[] keyValue = line.split("=");
            String key = keyValue[0];
            String value = keyValue[1];

            switch (key) {
                case "ID":
                    userBuilder.withId(value);
                    break;
                case "FIRSTNAME":
                    userBuilder.withFirstName(value);
                    break;
                case "LASTNAME":
                    userBuilder.withLastName(value);
                    break;
                case "BSN":
                    userBuilder.withBSN(value);
                    break;
                case "BIRTHDATE":
                    userBuilder.withBirthDate(Util.parseDate(value));
                    break;
                case "TIMESTAMP_RECEIVED":
                    builder.withReceived(Util.parseDate(value));
                    break;
            }
        }
        builder.withUser(userBuilder.build());
        return builder.build();
    }

    public static final class Builder {
        private User user;
        private Date received;

        public Builder() {
        }

        public Builder withUser(User val) {
            user = val;
            return this;
        }

        public Builder withReceived(Date val) {
            received = val;
            return this;
        }

        public SendUserDataPacket build() {
            return new SendUserDataPacket(this);
        }
    }
}
