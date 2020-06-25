package org.dockbox.corona.core.packets;

import org.dockbox.corona.core.model.UserData;
import org.dockbox.corona.core.util.Util;

import java.util.Date;

public class SendUserDataPacket extends Packet {

    public static final SendUserDataPacket EMPTY = new SendUserDataPacket(null, null);

    private final UserData userData;
    private final Date received;

    public SendUserDataPacket(UserData userData, Date received) {
        this.userData = userData;
        this.received = received;
    }

    private SendUserDataPacket(Builder builder) {
        userData = builder.userData;
        received = builder.received;
    }

    @Override
    public String getHeader() {
        return "SEND::USER_DATA";
    }

    @Override
    public String serialize() {
        return new StringBuilder()
                .append("ID=").append(userData.getId())
                .append("\nFIRSTNAME=").append(userData.getFirstName())
                .append("\nLASTNAME=").append(userData.getLastName())
                .append("\nBSN=").append(userData.getBSN())
                .append("\nBIRTHDATE=").append(Util.parseDateString(userData.getBirthDate()))
                .append("\nTIMESTAMP_RECEIVED=").append(Util.parseDateString(received))
                .toString();
    }

    @Override
    public SendUserDataPacket deserialize(String message) {
        String[] lines = message.split("\n");
        Builder builder = new Builder();
        UserData.Builder userBuilder = new UserData.Builder();
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

    public UserData getUserData() {
        return userData;
    }

    public Date getReceived() {
        return received;
    }

    public static final class Builder {
        private UserData userData;
        private Date received;

        public Builder() {
        }

        public Builder withUser(UserData val) {
            userData = val;
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
