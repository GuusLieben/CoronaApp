package org.dockbox.corona.core.util;

import org.dockbox.corona.core.packets.Packet;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

public class PacketSecurityUtil {

    public static final String INVALID = "$InvalidatedContent";

    public static String getHash(String unencryptedPacket) {
        String[] packetLines = unencryptedPacket.split("\n");
        String hashLine = packetLines[packetLines.length - 1];
        if (hashLine.startsWith("HASH::")) return hashLine.replaceFirst("HASH::", "");
        return PacketSecurityUtil.INVALID;
    }

    public static String getContent(String unencryptedPacket) {
        String[] packetLines = unencryptedPacket.split("\n");
        String[] contentLines = Arrays.copyOfRange(packetLines, 1, packetLines.length - 1);
        return String.join("\n", contentLines);
    }

    public static String getHeader(String unencryptedPacket) {
        return unencryptedPacket.split("\n")[0];
    }

    public static String generateHash(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] messageDigest = md.digest(content.getBytes(StandardCharsets.UTF_8));

            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    public static Optional<KeyPair> generateKeyPair() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            return Optional.ofNullable(kpg.generateKeyPair());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
    public static boolean isUnmodified(String content, String hash) {
        return generateHash(content).equals(hash);
    }
    public static byte[] encrypt(String content, PrivateKey privKey) {
        try {
            Cipher encrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            encrypt.init(Cipher.ENCRYPT_MODE, privKey);
            return encrypt.doFinal(toByteArray(content));
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            return new byte[0];
        }
    }

    public static String decrypt(byte[] encrypted, PublicKey pubKey) {
        try {
            Cipher decrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            decrypt.init(Cipher.DECRYPT_MODE, pubKey);
            return toString(decrypt.doFinal(encrypted));
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            return PacketSecurityUtil.INVALID;
        }
    }
    public static PrivateKey storePubAndGetKey(File out) {
        Optional<KeyPair> optionalKeyPair = generateKeyPair();
        if (optionalKeyPair.isPresent()) {
            KeyPair keyPair = optionalKeyPair.get();
            try {
                OutputStream stream;
                stream = new FileOutputStream(out);
                byte[] encoded = keyPair.getPublic().getEncoded();
                System.out.println("Encoded before store : " + Arrays.toString(encoded));
                stream.write(encoded);
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return keyPair.getPrivate();
        }
        return null;
    }

    public static PublicKey getPublicKeyFromFile(File file) {
        Path path = Paths.get(file.toURI());
        try {
            byte[] bytes = Files.readAllBytes(path);
            X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(ks);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }
}
