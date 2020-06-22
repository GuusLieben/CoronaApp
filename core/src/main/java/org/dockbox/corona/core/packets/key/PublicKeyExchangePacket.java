package org.dockbox.corona.core.packets.key;

import org.dockbox.corona.core.packets.Packet;
import org.dockbox.corona.core.util.Util;

import java.security.KeyException;
import java.security.PublicKey;

public class PublicKeyExchangePacket extends Packet {

    public static final PublicKeyExchangePacket EMPTY = new PublicKeyExchangePacket(null);

    private final PublicKey publicKey;

    public PublicKeyExchangePacket(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String getHeader() {
        return "KEY::PUBLIC";
    }

    @Override
    public String serialize() {
        return "KEY::PUBLIC::" + Util.encodeKeyToBase64(publicKey);
    }

    @Override
    public Packet deserialize(String message) {
        String base64EncodedKey = message.replaceFirst("KEY::PUBLIC::", "");
        try {
            return new PublicKeyExchangePacket((PublicKey) Util.decodeBase64ToKey(base64EncodedKey, true));
        } catch (KeyException e) {
            return null;
        }
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}
