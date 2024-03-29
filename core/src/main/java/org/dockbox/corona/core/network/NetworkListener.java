package org.dockbox.corona.core.network;

import org.dockbox.corona.core.packets.key.ExtraPacketHeader;
import org.dockbox.corona.core.packets.key.PublicKeyExchangePacket;
import org.dockbox.corona.core.packets.key.SessionKeyExchangePacket;
import org.dockbox.corona.core.packets.key.SessionKeyOkExchangePacket;
import org.dockbox.corona.core.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class NetworkListener extends NetworkCommunicator {

    private final PrivateKey privateKey;
    private final Map<String, PublicKey> publicKeyMap = new ConcurrentHashMap<>();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public NetworkListener(PrivateKey privateKey) {
        super(privateKey);
        this.privateKey = privateKey;
    }

    public void listen() {
        log.info("Opening socket on port " + getSocket().getLocalPort());
        while (true) {
            try {
                log.info("Preparing buffer");
                byte[] buffer = new byte[Util.INITIAL_KEY_BLOCK_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                log.info("Waiting for packets on port " + getSocket().getLocalPort());
                getSocket().receive(packet);
                String rawPacket = Util.convertPacketBytes(packet.getData());
                if (Util.INVALID.equals(rawPacket)) continue;

                String remoteLocation = String.format("%s:%d", packet.getAddress().getHostAddress(), packet.getPort());
                log.info("Received packet from " + remoteLocation);

                if (rawPacket.startsWith(ExtraPacketHeader.KEY_PREFIX.getValue())) {
                    log.info("Key exchange packet, validating");
                    // Plain text, no decryption needed
                    if (rawPacket.startsWith(PublicKeyExchangePacket.EMPTY.getHeader())) {
                        PublicKeyExchangePacket pkep = PublicKeyExchangePacket.EMPTY.deserialize(rawPacket);
                        if (pkep != null) {
                            log.info("Public key OK");
                            publicKeyMap.put(remoteLocation, pkep.getPublicKey());
                            sendDatagram(ExtraPacketHeader.KEY_OK.getValue(), true, packet.getAddress(), packet.getPort(), false, null);
                        } else {
                            log.info("Public key rejected");
                            sendDatagram(ExtraPacketHeader.KEY_REJECTED.getValue(), true, packet.getAddress(), packet.getPort(), false, null);
                        }

                    } else if (rawPacket.startsWith(SessionKeyExchangePacket.EMPTY.getHeader())) {
                        SessionKeyExchangePacket skep = SessionKeyExchangePacket.EMPTY.deserialize(rawPacket);
                        if (skep != null && Util.sessionKeyIsValid(skep.getSessionKey()) && publicKeyMap.containsKey(remoteLocation)) {
                            log.info("Session key OK");
                            SessionKeyOkExchangePacket skoep = new SessionKeyOkExchangePacket(skep.getSessionKey());
                            sessions.put(remoteLocation, new Session(publicKeyMap.get(remoteLocation), skoep.getSessionKey(), packet.getAddress(), packet.getPort()));
                            sendPacket(skoep, true, true, packet.getAddress(), packet.getPort(), false, null, null);
                        } else {
                            log.info("Session key rejected");
                            sendDatagram(ExtraPacketHeader.KEY_REJECTED.getValue(), true, packet.getAddress(), packet.getPort(), false, null);
                        }
                    }

                } else {
                    log.info("Encrypted packet, decrypting in session");
                    Session session;
                    if (sessions.containsKey(remoteLocation)) {
                        // Prepared, encrypted
                        session = sessions.get(remoteLocation);
                    } else {
                        // Not prepared, plain
                        session = new Session(null, null, packet.getAddress(), packet.getPort());
                    }
                    new Thread(session.injectPacket(rawPacket)).start();
                }
            } catch (IOException | RuntimeException e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Session key is not applicable for public listeners,
    // this functionality is available in the Session type
    @Override
    protected SecretKey getSessionKey() {
        throw new UnsupportedOperationException("This operation is not supported for network listeners");
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public Map<String, Session> getSessions() {
        return sessions;
    }

    protected abstract void handlePacket(String rawPacket, Session session);

    protected class Session implements Runnable {

        private final PublicKey remotePublicKey;
        private final SecretKey sessionKey;
        private final InetAddress remote;
        private final int remotePort;
        private final Logger log;

        private String rawPacket;

        public Session injectPacket(String rawPacket) {
            this.rawPacket = rawPacket;
            return this;
        }

        public Session(PublicKey remotePublicKey, SecretKey sessionKey, InetAddress remote, int remotePort) {
            this.remotePublicKey = remotePublicKey;
            this.sessionKey = sessionKey;
            this.remote = remote;
            this.remotePort = remotePort;
            this.log = LoggerFactory.getLogger(remote.getHostAddress() + "-session");
        }

        public SecretKey getSessionKey() {
            return sessionKey;
        }

        public InetAddress getRemote() {
            return remote;
        }

        public int getRemotePort() {
            return remotePort;
        }

        public String getRawPacket() {
            return rawPacket;
        }

        public PublicKey getRemotePublicKey() {
            return remotePublicKey;
        }

        @Override
        public void run() {
            log.info("Session started");
            handlePacket(getRawPacket(), this);
        }
    }

}
