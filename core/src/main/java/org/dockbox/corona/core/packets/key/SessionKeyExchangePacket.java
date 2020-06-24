package org.dockbox.corona.core.packets.key;

import org.dockbox.corona.core.packets.Packet;
import org.dockbox.corona.core.util.Util;

import javax.crypto.SecretKey;
import java.security.KeyException;

public class SessionKeyExchangePacket extends Packet {

    public static final SessionKeyExchangePacket EMPTY = new SessionKeyExchangePacket(null);

    private final SecretKey sessionKey;

    public SessionKeyExchangePacket(SecretKey sessionKey) {
        this.sessionKey = sessionKey;
    }

    @Override
    public String getHeader() {
        return "KEY::SESSION";
    }

    @Override
    public String serialize() {
        return "KEY::SESSION::" + Util.encodeKeyToBase64(sessionKey);
    }

    @Override
    public SessionKeyExchangePacket deserialize(String message) {
        String base64EncodedKey = message.replaceFirst("KEY::SESSION::", "");
        try {
            return new SessionKeyExchangePacket((SecretKey) Util.decodeBase64ToKey(base64EncodedKey, false));
        } catch (KeyException e) {
            e.printStackTrace();
            return null;
        }
    }

    public SecretKey getSessionKey() {
        return sessionKey;
    }
}
