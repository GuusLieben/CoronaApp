package org.dockbox.corona.core.packets.key;

import org.dockbox.corona.core.packets.Packet;
import org.dockbox.corona.core.util.Util;

import javax.crypto.SecretKey;
import java.security.KeyException;

public class SessionKeyOkExchangePacket extends Packet {

    public static final SessionKeyOkExchangePacket EMPTY = new SessionKeyOkExchangePacket(null);

    private final SecretKey sessionKey;

    public SessionKeyOkExchangePacket(SecretKey sessionKey) {
        this.sessionKey = sessionKey;
    }

    @Override
    public String getHeader() {
        return "KEY::SESSION_OK";
    }

    @Override
    public String serialize() {
        return "KEY::SESSION_OK::" + Util.encodeKeyToBase64(sessionKey);
    }

    @Override
    public SessionKeyOkExchangePacket deserialize(String message) {
        String base64EncodedKey = message.replaceFirst("KEY::SESSION_OK::", "");
        try {
            return new SessionKeyOkExchangePacket((SecretKey) Util.decodeBase64ToKey(base64EncodedKey, false));
        } catch (KeyException e) {
            e.printStackTrace();
            return null;
        }
    }

    public SecretKey getSessionKey() {
        return sessionKey;
    }
}
