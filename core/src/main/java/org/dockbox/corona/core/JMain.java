package org.dockbox.corona.core;

import org.dockbox.corona.core.packets.SendContactConfPacket;
import org.dockbox.corona.core.util.Util;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.Date;
import java.time.Instant;
import java.util.Arrays;

public class JMain {

    public static void main(String[] args) {
        Util.generateKeyPair().ifPresent(clientKeyPair -> {
            Util.generateKeyPair().ifPresent(serverKeyPair -> {
                // Performed by client, sent to server
                PublicKey clientPublic = clientKeyPair.getPublic();
                // Performed by client, sent to server
                SecretKey clientSession = Util.generateSessionKey().get();
                // Performed by server, sent to client
                PrivateKey serverPrivate = serverKeyPair.getPrivate();
                byte[] guessedClientSecret = Util.decryptSessionKey(clientSession);
                System.out.println("Guessed client : " + Arrays.toString(guessedClientSecret));
                SecretKey serverSession = Util.generateSessionKey(guessedClientSecret).get();
                // Performed by client
                PrivateKey clientPrivate = clientKeyPair.getPrivate();
                byte[] guessedServerSecret = Util.decryptSessionKey(serverSession);

                System.out.println("Session key validation status : " + Arrays.equals(guessedClientSecret, guessedServerSecret));

                SendContactConfPacket sccp = new SendContactConfPacket("69", "420", Date.from(Instant.now()), Date.from(Instant.now()));
                String encryptedPacket = Util.encryptPacket(sccp, clientPrivate, clientSession);

                // Performed by server
                String decryptedPacket = Util.decryptPacket(encryptedPacket, clientPublic, clientSession);
                String hash = Util.getHash(decryptedPacket);
                String content = Util.getContent(decryptedPacket);
                System.out.println("Hash = " + hash);
                System.out.println("Protocol = " + Util.getHeader(decryptedPacket));
                System.out.println("Content = " + content);
                System.out.println("Is clean = " + Util.isUnmodified(content, hash));
            });
        });
    }

}
