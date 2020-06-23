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

public abstract class NetworkCommunicator {

    protected Logger log;

    protected final PrivateKey privateKey;

    protected NetworkCommunicator(PrivateKey privateKey) {
        this.log = LoggerFactory.getLogger(NetworkCommunicator.class);
        this.privateKey = privateKey;
    }

    protected abstract SecretKey getSessionKey();
    protected abstract DatagramSocket getSocket();

    public String sendPacket(Packet packet, boolean skipDecrypt, boolean skipEncrypt, InetAddress remoteHost, int remotePort, boolean listenForResponse) {
        return sendDatagram(skipEncrypt ? packet.serialize() : Util.encryptPacket(packet, this.privateKey, getSessionKey()), skipDecrypt, remoteHost, remotePort, listenForResponse);
    }

    public String sendPacket(Packet packet, boolean skipDecrypt, boolean skipEncrypt, InetAddress remoteHost, int remotePort) {
        return sendPacket(packet, skipDecrypt, skipEncrypt, remoteHost, remotePort, true);
    }

    public String sendDatagram(String data, boolean skipDecrypt, InetAddress remoteHost, int remotePort, boolean listenForResponse) {
        try {
            byte[] buffer = data.getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, remoteHost, remotePort);
            log.info(String.format("Sending '%s' to remote", data));
            getSocket().send(datagramPacket);

            if (listenForResponse) {
                byte[] receiveBuffer = new byte[Util.INITIAL_KEY_BLOCK_SIZE];
                datagramPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

                log.info("Listening for response from remote");
                getSocket().receive(datagramPacket);
                log.info("Received '" + data + "' from remote");
                String rawPacket = Util.convertPacketBytes(datagramPacket.getData());
                if (skipDecrypt) return rawPacket;
                else return Util.decryptPacket(rawPacket, privateKey, getSessionKey());

            } else return "";
        } catch (IOException e) {
            return Util.INVALID;
        }
    }

    public String sendDatagram(String data, boolean skipDecrypt, InetAddress remoteHost, int remotePort) {
        return sendDatagram(data, skipDecrypt, remoteHost, remotePort, true);
    }

}
