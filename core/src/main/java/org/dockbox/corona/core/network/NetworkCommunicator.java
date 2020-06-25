package org.dockbox.corona.core.network;

import org.dockbox.corona.core.packets.Packet;
import org.dockbox.corona.core.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.PrivateKey;
import java.security.PublicKey;

public abstract class NetworkCommunicator {

    protected Logger log;

    protected final PrivateKey privateKey;

    protected NetworkCommunicator(PrivateKey privateKey) {
        this.log = LoggerFactory.getLogger(NetworkCommunicator.class);
        this.privateKey = privateKey;
    }

    protected abstract SecretKey getSessionKey();
    protected abstract DatagramSocket getSocket();

    public String sendPacket(Packet packet, boolean skipDecrypt, boolean skipEncrypt, InetAddress remoteHost, int remotePort, boolean listenForResponse, PublicKey foreignPublicKey, SecretKey sessionKey) {
        return sendDatagram(skipEncrypt ? packet.serialize() : Util.encryptPacket(packet, this.privateKey, sessionKey), skipDecrypt, remoteHost, remotePort, listenForResponse, foreignPublicKey);
    }

    public String sendPacket(Packet packet, boolean skipDecrypt, boolean skipEncrypt, InetAddress remoteHost, int remotePort, boolean listenForResponse, PublicKey foreignPublicKey) {
        return sendPacket(packet, skipDecrypt, skipEncrypt, remoteHost, remotePort, listenForResponse, foreignPublicKey, getSessionKey());
    }

    public String sendPacket(Packet packet, boolean skipDecrypt, boolean skipEncrypt, InetAddress remoteHost, int remotePort, PublicKey foreignPublicKey) {
        return sendPacket(packet, skipDecrypt, skipEncrypt, remoteHost, remotePort, true, foreignPublicKey);
    }

    public String sendDatagram(String data, boolean skipDecrypt, InetAddress remoteHost, int remotePort, boolean listenForResponse, PublicKey foreignPublicKey) {
        try {
            byte[] buffer = data.getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, remoteHost, remotePort);
            log.info(String.format("Sending '%s' to remote at '%s:%d'", data, remoteHost.getHostAddress(), remotePort));
            getSocket().send(datagramPacket);

            if (listenForResponse) {
                byte[] receiveBuffer = new byte[Util.INITIAL_KEY_BLOCK_SIZE];
                datagramPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

                log.info("Listening for response from remote on port " + getSocket().getLocalPort());
                getSocket().receive(datagramPacket);
                log.info("Received '" + data + "' from remote");
                String rawPacket = Util.convertPacketBytes(datagramPacket.getData());
                if (Util.INVALID.equals(rawPacket)) return Util.INVALID;
                if (skipDecrypt) return rawPacket;
                else return Util.decryptPacket(rawPacket, foreignPublicKey, getSessionKey()).replaceAll("\n\n", "\n");

            } else return "";
        } catch (IOException e) {
            return Util.INVALID;
        }
    }

    public String sendDatagram(String data, boolean skipDecrypt, InetAddress remoteHost, int remotePort, PublicKey foreignPublicKey) {
        return sendDatagram(data, skipDecrypt, remoteHost, remotePort, true, foreignPublicKey);
    }

}
