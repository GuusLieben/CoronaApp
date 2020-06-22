package org.dockbox.corona.core.network;

import org.dockbox.corona.core.packets.key.KeyHeaders;
import org.dockbox.corona.core.packets.key.PublicKeyExchangePacket;
import org.dockbox.corona.core.packets.key.SessionKeyExchangePacket;
import org.dockbox.corona.core.packets.key.SessionKeyOkExchangePacket;
import org.dockbox.corona.core.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.activation.ActivateFailedException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

public class TCPConnection extends NetworkCommunicator {

    protected final Logger log;

    protected PublicKey foreignPublicKey;
    protected final PrivateKey privateKey;
    protected final PublicKey publicKey;
    protected final SecretKey sessionKey;

    protected final InetAddress remoteHost;
    protected final int remotePort;
    protected final DatagramSocket socket;
    protected final boolean isServer;

    public TCPConnection(PrivateKey privateKey, PublicKey publicKey, String remoteHost, int remotePort, boolean isServer) throws IOException, InstantiationException {
        super(privateKey);
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
            this.sessionKey = optionalSessionKey.get();
        } else throw exceptionSupplier.get();
    }

    public void initiateKeyExchange() throws ActivateFailedException {
        log.info("Initiating key exchange with remote");
        PublicKeyExchangePacket pkep = new PublicKeyExchangePacket(publicKey);
        log.info("Sending public key (self) to remote");
        String response = sendPacket(pkep, true, remoteHost, remotePort);
        if (
                (isServer && response.startsWith(pkep.getHeader())) // If we are a server, make sure we receive the public key of the client
                        || (KeyHeaders.KEY_OK.getValue().equals(response) && !isServer) // If we are a client, we already have the public key of the server
        ) {
            log.info("Response OK");
            if (isServer && response.startsWith(pkep.getHeader())) {
                log.info("Received public key from remote");
                PublicKeyExchangePacket pkepForeign = PublicKeyExchangePacket.EMPTY.deserialize(response);
                this.foreignPublicKey = pkepForeign.getPublicKey();
            } // Else already handled by upper condition

            SessionKeyExchangePacket skep = new SessionKeyExchangePacket(sessionKey);
            log.info("Sending session key (self) to remote");
            response = sendPacket(skep, true, remoteHost, remotePort);

            if (response.startsWith(SessionKeyOkExchangePacket.EMPTY.getHeader())) {
                log.info("Received session key OK from remote");
                SessionKeyOkExchangePacket skoep = SessionKeyOkExchangePacket.EMPTY.deserialize(response);
                if (!Arrays.equals(this.sessionKey.getEncoded(), skoep.getSessionKey().getEncoded()))
                    throw new ActivateFailedException("Could not activate connection (session key mismatch)");
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

    @Override
    public SecretKey getSessionKey() {
        return sessionKey;
    }

    public InetAddress getRemoteHost() {
        return remoteHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    @Override
    public DatagramSocket getSocket() {
        return socket;
    }

}
