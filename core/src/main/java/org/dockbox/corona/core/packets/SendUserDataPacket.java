package org.dockbox.corona.core.packets;

import org.dockbox.corona.core.model.User;
import org.dockbox.corona.core.util.CommonUtil;

import java.sql.Time;

public class SendUserDataPacket extends Packet {

    private final User user;
    private final Time received;

    public SendUserDataPacket(User user, Time received, Time sent) {
        this.user = user;
        this.received = received;
    }

    private SendUserDataPacket(Builder builder) {
        user = builder.user;
        received = builder.received;
    }

    @Override
    public String serialize() {
        return new StringBuilder()
                .append("ID=").append(user.getId())
                .append("\nFIRSTNAME=").append(user.getFirstName())
                .append("\nLASTNAME=").append(user.getLastName())
                .append("\nBSN=").append(user.getBSN())
                .append("\nBIRTHDATE=").append(CommonUtil.parseDateString(user.getBirthDate()))
                .append("\nRECEIVED=").append(CommonUtil.parseTimeString(received))
                .toString();
    }

    @Override
    public Packet deserialize(String message) {
        return null;
    }

    public static final class Builder {
        private User user;
        private Time received;
        private Time sent;

        public Builder() {
        }

        public Builder withUser(User val) {
            user = val;
            return this;
        }

        public Builder withReceived(Time val) {
            received = val;
            return this;
        }

        public Builder withSent(Time val) {
            sent = val;
            return this;
        }

        public SendUserDataPacket build() {
            return new SendUserDataPacket(this);
        }
    }
}
