package org.dockbox.corona.core.packets;

import org.dockbox.corona.core.util.CommonUtil;

import java.sql.Time;

public class ConfirmPacket<P extends Packet> extends Packet {

    private final P packet;
    private final Time confirmed;

    public ConfirmPacket(P packet, Time confirmed) {
        this.packet = packet;
        this.confirmed = confirmed;
    }

    private ConfirmPacket(Builder<P> builder) {
        packet = builder.packet;
        confirmed = builder.confirmed;
    }

    @Override
    public String serialize() {
        return new StringBuilder()
                .append(packet.serialize())
                .append("TIMESTAMP_CONFIRMED=").append(CommonUtil.parseTimeString(confirmed))
                .toString();
    }

    @Override
    public Packet deserialize(String message) {
        return null;
    }


    public static final class Builder<P extends Packet> {
        private P packet;
        private Time confirmed;

        public Builder() {
        }

        public Builder<P> withPacket(P val) {
            packet = val;
            return this;
        }

        public Builder<P> withConfirmed(Time val) {
            confirmed = val;
            return this;
        }

        public ConfirmPacket<P> build() {
            return new ConfirmPacket<>(this);
        }
    }
}
