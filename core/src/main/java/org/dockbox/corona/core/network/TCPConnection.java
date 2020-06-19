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

    private final Logger log;

    private PublicKey foreignPublicKey;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final SecretKey sessionKeySelf;
    private SecretKey sessionKeyForeign;

    private final InetAddress remoteHost;
    private final int remotePort;
    private final DatagramSocket socket;
    private final boolean isServer;

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

    public String sendPacket(Packet packet) {
        return sendDatagram(Util.encryptPacket(packet, this.privateKey, this.sessionKeySelf));
    }

    public String sendDatagram(String data) {
    }

    public void initiateKeyExchange() throws ActivateFailedException {
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
