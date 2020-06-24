package org.dockbox.corona.core.packets;

import org.dockbox.corona.core.util.Util;

import java.util.Date;
import java.util.Arrays;

public class ConfirmPacket<P extends Packet> extends Packet {

    private final P packet;
    private final Date confirmed;

    public ConfirmPacket(P packet, Date confirmed) {
        this.packet = packet;
        this.confirmed = confirmed;
    }

    private ConfirmPacket(Builder<P> builder) {
        packet = builder.packet;
        confirmed = builder.confirmed;
    }

    @Override
    public String getHeader() {
        return "CONFIRM::" + packet.getHeader().split("::")[1];
    }

    @Override
    public String serialize() {
        String packetSer = new StringBuilder()
                .append(packet.serialize())
                .toString();

        packetSer = String.join("\n",
                Arrays.stream(packetSer.split("\n"))
                        .filter(line -> !line.startsWith("TIMESTAMP_CONTACT") && line.startsWith("TIMESTAMP"))
                        .toArray(String[]::new));

        return new StringBuilder()
                .append(packetSer)
                .append("\nTIMESTAMP_CONFIRMED=").append(Util.parseDateString(confirmed))
                .toString();
    }

    @SuppressWarnings("unchecked")
    public ConfirmPacket<P> deserialize(String message, P empty) {
        String[] lines = message.split("\n");
        Builder<P> builder = new Builder<P>();

        String confirmedStamp = lines[lines.length-1];
        builder.withConfirmed(Util.parseDate(confirmedStamp.split("=")[1]));

        String[] child = Arrays.stream(lines).filter(line -> !line.startsWith("TIMESTAMP_CONFIRMED")).toArray(String[]::new);
        builder.withPacket((P) empty.deserialize(String.join("\n", child)));

        return builder.build();
    }

    @Override
    public ConfirmPacket<P> deserialize(String message) {
        throw new UnsupportedOperationException("Cannot deserialize confirm packet without empty child");
    }


    public static final class Builder<P extends Packet> {
        private P packet;
        private Date confirmed;

        public Builder() {
        }

        public Builder<P> withPacket(P val) {
            packet = val;
            return this;
        }

        public Builder<P> withConfirmed(Date val) {
            confirmed = val;
            return this;
        }

        public ConfirmPacket<P> build() {
            return new ConfirmPacket<>(this);
        }
    }
}
