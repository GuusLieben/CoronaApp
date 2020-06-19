package org.dockbox.corona.core.packets;

public abstract class Packet {

    public static final String HASH_PREFIX = "HASH::";

    public abstract String getHeader();

    public abstract String serialize();

    public abstract Packet deserialize(String message);

}
