package org.dockbox.corona.core.network;

import org.dockbox.corona.core.packets.key.PublicKeyExchangePacket;
import org.dockbox.corona.core.packets.key.SessionKeyExchangePacket;
import org.dockbox.corona.core.packets.key.SessionKeyOkExchangePacket;
import org.dockbox.corona.core.util.Util;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class NetworkListener extends NetworkCommunicator {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final DatagramSocket socket;
    private final Map<String, SessionHandler> sessions = new ConcurrentHashMap<>();

    public NetworkListener(PrivateKey privateKey, PublicKey publicKey) throws IOException, InstantiationException {
        super(privateKey);
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.socket = new DatagramSocket();
    }

    public void listen() {
        while (true) {
            try {
                byte[] buffer = new byte[Util.INITIAL_KEY_BLOCK_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String rawPacket = Util.convertPacketBytes(packet.getData());
                String remoteLocation = String.format("%s:%d", packet.getAddress().getHostAddress(), packet.getPort());

                if (rawPacket.startsWith("KEY::")) {
                    // Plain text, no decryption needed
                    if (rawPacket.startsWith(PublicKeyExchangePacket.EMPTY.getHeader())) {
                        PublicKeyExchangePacket pkep = (PublicKeyExchangePacket) PublicKeyExchangePacket.EMPTY.deserialize(rawPacket);
                        if (pkep != null) sendDatagram("KEY::OK", true, packet.getAddress(), packet.getPort());

                        else sendDatagram("KEY::REJECT", true, packet.getAddress(), packet.getPort());

                    } else if (rawPacket.startsWith(SessionKeyExchangePacket.EMPTY.getHeader())) {
                        SessionKeyExchangePacket skep = (SessionKeyExchangePacket) SessionKeyExchangePacket.EMPTY.deserialize(rawPacket);
                        if (skep != null && Util.sessionKeyIsValid(skep.getSessionKey(), privateKey)) {
                            SessionKeyOkExchangePacket skoep = new SessionKeyOkExchangePacket(skep.getSessionKey());
                            sendPacket(skoep, true, packet.getAddress(), packet.getPort());
                            sessions.put(remoteLocation, new SessionHandler(skoep.getSessionKey(), packet.getAddress(), packet.getPort()));

                        } else sendDatagram("KEY::REJECT", true, packet.getAddress(), packet.getPort());
                    }

                } else {
                    // Encrypted
                    SessionHandler session = sessions.get(remoteLocation);
                    new Thread(session.injectPacket(rawPacket)).start();
                }
            } catch (IOException | RuntimeException e) {
                log.error(e.getMessage());
            }
        }
    }

    protected abstract void handlePacket(String rawPacket);

    protected class SessionHandler implements Runnable {

        private final SecretKey sessionKey;
        private final InetAddress remote;
        private final int remotePort;

        private String rawPacket;

        public SessionHandler injectPacket(String rawPacket) {
            this.rawPacket = rawPacket;
            return this;
        }

        public SessionHandler(SecretKey sessionKey, InetAddress remote, int remotePort) {
            this.sessionKey = sessionKey;
            this.remote = remote;
            this.remotePort = remotePort;
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

        @Override
        public void run() {
            handlePacket(getRawPacket());
        }
    }

}
