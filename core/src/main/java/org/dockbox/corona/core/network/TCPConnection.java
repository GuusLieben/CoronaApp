package org.dockbox.corona.core.network;

import org.dockbox.corona.core.packets.Packet;
import org.dockbox.corona.core.packets.key.PublicKeyExchangePacket;
import org.dockbox.corona.core.packets.key.SessionKeyExchangePacket;
import org.dockbox.corona.core.packets.key.SessionKeyOkExchangePacket;
import org.dockbox.corona.core.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.activation.ActivateFailedException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Optional;
import java.util.function.Supplier;

public class TCPConnection {

    protected final Logger log;

    protected PublicKey foreignPublicKey;
    protected final PrivateKey privateKey;
    protected final PublicKey publicKey;
    protected final SecretKey sessionKeySelf;
    protected SecretKey sessionKeyForeign;

    protected final InetAddress remoteHost;
    protected final int remotePort;
    protected final DatagramSocket socket;
    protected final boolean isServer;

    public TCPConnection(PrivateKey privateKey, PublicKey publicKey, String remoteHost, int remotePort, boolean isServer) throws IOException, InstantiationException {
        this.log = LoggerFactory.getLogger(String.format("TCP:%s:%d", remoteHost, remotePort));

        log.info("Initiating new TCP connection");

        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.remoteHost = InetAddress.getByName(remoteHost);
        this.remotePort = remotePort;
        this.socket = new DatagramSocket(remotePort, this.remoteHost);
        this.isServer = isServer;

        Supplier<InstantiationException> exceptionSupplier = () -> new InstantiationException("Failed to generate or obtain key");
        if (!isServer) {
            log.info("Connection started by client");
            log.info("Requesting server public key from resource");
            Optional<PublicKey> optionalForeignPublicKey = Util.getPublicKeyFromFile(new File("central_cli.pub"));
            if (optionalForeignPublicKey.isPresent()) {
                log.info("Obtained server public key from resource");
                this.foreignPublicKey = optionalForeignPublicKey.get();
            } else throw exceptionSupplier.get();
        }

        log.info("Generating session key (self)");
        Optional<SecretKey> optionalSessionKey = Util.generateSessionKey();
        if (optionalSessionKey.isPresent()) {
            log.info("Obtained session key (self)");
            this.sessionKeySelf = optionalSessionKey.get();
        } else throw exceptionSupplier.get();
    }

    public String sendPacket(Packet packet, boolean skipDecrypt) {
        return sendDatagram(Util.encryptPacket(packet, this.privateKey, this.sessionKeySelf), skipDecrypt);
    }

    public String sendDatagram(String data, boolean skipDecrypt) {
        try {
            byte[] buffer = data.getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, this.remoteHost, this.remotePort);
            log.info(String.format("Sending '%s' to remote", data));
            socket.send(datagramPacket);

            byte[] receiveBuffer = new byte[Util.INITIAL_KEY_BLOCK_SIZE];
            datagramPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            log.info("Listening for response from remote");
            socket.receive(datagramPacket);
            log.info("Received '" + data + "' from remote");
            String rawPacket = Util.convertPacketBytes(datagramPacket.getData());
            if (skipDecrypt) return rawPacket;
            else return Util.decryptPacket(rawPacket, privateKey, sessionKeyForeign);
        } catch (IOException e) {
            return Util.INVALID;
        }
    }

    public void initiateKeyExchange() throws ActivateFailedException {
        log.info("Initiating key exchange with remote");
        PublicKeyExchangePacket pkep = new PublicKeyExchangePacket(publicKey);
        log.info("Sending public key (self) to remote");
        String response = sendPacket(pkep, true);
        if (
                (isServer && response.startsWith(pkep.getHeader())) // If we are a server, make sure we receive the public key of the client
                        || ("KEY::OK".equals(response) && !isServer) // If we are a client, we already have the public key of the server
        ) {
            log.info("Response OK");
            if (isServer && response.startsWith(pkep.getHeader())) {
                log.info("Received public key from remote");
                PublicKeyExchangePacket pkepForeign = (PublicKeyExchangePacket) PublicKeyExchangePacket.EMPTY.deserialize(response);
                this.foreignPublicKey = pkepForeign.getPublicKey();
            } // Else already handled by upper condition

            SessionKeyExchangePacket skep = new SessionKeyExchangePacket(sessionKeySelf);
            log.info("Sending session key (self) to remote");
            response = sendPacket(skep, true);

            if (response.startsWith(SessionKeyOkExchangePacket.EMPTY.getHeader())) {
                log.info("Received session key OK from remote");
                SessionKeyOkExchangePacket skoep = (SessionKeyOkExchangePacket) SessionKeyOkExchangePacket.EMPTY.deserialize(response);
                this.sessionKeyForeign = skoep.getSessionKey();
            } else throw new ActivateFailedException("Could not activate connection (session key rejected)");

        } else throw new ActivateFailedException("Could not activate connection (public key rejected)");
    }

    public PublicKey getForeignPublicKey() {
        return foreignPublicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public SecretKey getSessionKeySelf() {
        return sessionKeySelf;
    }

    public SecretKey getSessionKeyForeign() {
        return sessionKeyForeign;
    }

    public InetAddress getRemoteHost() {
        return remoteHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

}
