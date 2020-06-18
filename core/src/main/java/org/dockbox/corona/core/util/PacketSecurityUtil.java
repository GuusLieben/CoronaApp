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
    public static final String HASH_ALGORITHM = "SHA-512";
    public static final String KEY_ALGORITHM = "RSA";
    public static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";

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
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
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

    private static String toString(byte[] bytes) {
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    private static byte[] toByteArray(String string) {
        return string.getBytes(StandardCharsets.ISO_8859_1);
    }

    public static Optional<KeyPair> generateKeyPair() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            kpg.initialize(2048);
            return Optional.ofNullable(kpg.generateKeyPair());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static String convertToBase64(Key key) {
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(key.getEncoded());
    }

    public static boolean isUnmodified(String content, String hash) {
        return generateHash(content).equals(hash);
    }

    public static byte[] encrypt(String content, PrivateKey privKey) {
        try {
            Cipher encrypt = Cipher.getInstance(CIPHER_ALGORITHM);
            encrypt.init(Cipher.ENCRYPT_MODE, privKey);
            return encrypt.doFinal(toByteArray(content));
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            return new byte[0];
        }
    }

    public static String decrypt(byte[] encrypted, PublicKey pubKey) {
        try {
            Cipher decrypt = Cipher.getInstance(CIPHER_ALGORITHM);
            decrypt.init(Cipher.DECRYPT_MODE, pubKey);
            return toString(decrypt.doFinal(encrypted));
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            return PacketSecurityUtil.INVALID;
        }
    }

    public static String encryptPacket(Packet p, PrivateKey privateKey) {
        String header = p.getHeader();
        String unencryptedContent = p.serialize();
        byte[] encryptedHeaderAndContent = encrypt(header + "\n" + unencryptedContent, privateKey);

        return new StringBuilder()
                .append(toString(encryptedHeaderAndContent)).append("\n")
                .append("HASH::").append(generateHash(unencryptedContent))
                .toString();
    }
    
    public static String decryptPacket(String encryptedPacket, PublicKey publicKey) {
        String[] packetParts = encryptedPacket.split("\n");
        String[] encryptedPacketParts = Arrays.copyOfRange(packetParts, 0, packetParts.length - 1);
        String hashLine = packetParts[packetParts.length - 1];
        String encryptedPacketPart = String.join("\n", encryptedPacketParts);
        String decryptedPacketPart = decrypt(toByteArray(encryptedPacketPart), publicKey);
        return decryptedPacketPart + "\n" + hashLine;
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
            KeyFactory kf = KeyFactory.getInstance(KEY_ALGORITHM);
            return kf.generatePublic(ks);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }
}
