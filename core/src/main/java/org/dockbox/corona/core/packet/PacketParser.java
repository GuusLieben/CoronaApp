package org.dockbox.corona.core.packet;

import org.dockbox.corona.core.models.Packet;

public abstract class PacketParser<T extends Packet> {

    public abstract String serialize(T model);

    public abstract T deserialize(String message);

}
