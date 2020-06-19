package org.dockbox.corona.core.network;

import org.dockbox.corona.core.packets.Packet;
import org.dockbox.corona.core.util.Util;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.DatagramPacket;
import java.security.PrivateKey;
import java.security.PublicKey;

public abstract class NetworkListener extends TCPConnection {

    public NetworkListener(PrivateKey privateKey, PublicKey publicKey, SecretKey foreignSecret) throws IOException, InstantiationException {
        super(privateKey, publicKey, null, -1, true);
        super.sessionKeyForeign = foreignSecret;
    }

    public void listen() {
        while (true) {
            try {
                byte[] buffer = new byte[Util.INITIAL_KEY_BLOCK_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                getSocket().receive(packet);
                String rawPacket = Util.convertPacketBytes(packet.getData());
                if (rawPacket.startsWith("KEY::")) {
                    // Key exchange
                } else {
                    String decryptedPacket = Util.decryptPacket(rawPacket, privateKey, sessionKeyForeign);
                    // TODO : Decide how to determine session key in global listener
                    // Other packets
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    public abstract void handlePacket(Packet packet);

}
