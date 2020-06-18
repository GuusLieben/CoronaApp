package org.dockbox.corona.core.packets;

public abstract class Packet {

    public abstract String serialize();

    public abstract Packet deserialize(String message);

}
