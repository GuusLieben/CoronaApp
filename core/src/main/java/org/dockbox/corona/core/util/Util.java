package org.dockbox.corona.core.util;

import org.dockbox.corona.core.packets.Packet;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
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
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

public class Util {

    public static final String INVALID = "$InvalidatedContent";
    public static final String HASH_ALGORITHM = "SHA-512";
    public static final String KEY_ALGORITHM = "RSA";
    public static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";
    public static final String SESSION_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    public static final String SESSION_KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA256";
    public static final String SESSION_KEY_ALGORITHM = "AES";
    public static final int INITIAL_KEY_BLOCK_SIZE = 4096;

    public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_PATTERN);

    public static String parseDateString(Date date) {
        return DATE_FORMAT.format(date);
    }

    public static Date parseDate(String date) {
        try {
            return DATE_FORMAT.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static String getHash(String unencryptedPacket) {
        String[] packetLines = unencryptedPacket.split("\n");
        String hashLine = packetLines[packetLines.length - 1];
        if (hashLine.startsWith(Packet.HASH_PREFIX)) return hashLine.replaceFirst(Packet.HASH_PREFIX, "");
        return Util.INVALID;
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

    public static String toString(byte[] bytes) {
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    public static byte[] toByteArray(String string) {
        return string.getBytes(StandardCharsets.ISO_8859_1);
    }

    public static Optional<KeyPair> generateKeyPair() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            kpg.initialize(INITIAL_KEY_BLOCK_SIZE);
            return Optional.ofNullable(kpg.generateKeyPair());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static boolean isUnmodified(String content, String hash) {
        return generateHash(content).equals(hash);
    }

    public static byte[] encrypt(String content, Key key, String algorithm) {
        byte[] toSend = new byte[0];
        try {
            Cipher encrypt = Cipher.getInstance(algorithm);
            encrypt.init(Cipher.ENCRYPT_MODE, key);
            byte[] contentB = toByteArray(content);
            toSend = encrypt.doFinal(contentB);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return toSend;
    }

    public static byte[] encrypt(String content, Key key) {
        return encrypt(content, key, CIPHER_ALGORITHM);
    }

    public static String decrypt(byte[] encrypted, Key key, String algorithm) {
        try {
            Cipher decrypt = Cipher.getInstance(algorithm);
            decrypt.init(Cipher.DECRYPT_MODE, key);
            return toString(decrypt.doFinal(encrypted));
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            return Util.INVALID;
        }
    }

    public static String decrypt(byte[] encrypted, Key key) {
        return decrypt(encrypted, key, CIPHER_ALGORITHM);
    }

    public static String encryptWithSessionKey(byte[] content, SecretKey sessionKey) {
        try {
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(SESSION_CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, sessionKey, ivspec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(content));
        } catch (GeneralSecurityException e) {
            return INVALID;
        }
    }

    public static byte[] decryptWithSessionKey(String content, SecretKey sessionKey) {
        try {
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(SESSION_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, sessionKey, ivspec);
            return cipher.doFinal(Base64.getDecoder().decode(content));
        } catch (GeneralSecurityException e) {
            return new byte[0];
        }
    }

    public static Optional<SecretKey> generateSessionKey(byte[] secret) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(SESSION_KEY_FACTORY_ALGORITHM);
            KeySpec spec = new PBEKeySpec(toString(secret).toCharArray(), secret, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);

            return Optional.of(new SecretKeySpec(tmp.getEncoded(), SESSION_KEY_ALGORITHM));
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<SecretKey> generateSessionKey() {
        SecureRandom r = new SecureRandom();
        byte[] aesKey = new byte[16];
        r.nextBytes(aesKey);
        return generateSessionKey(aesKey);
    }

    public static byte[] decryptSessionKey(SecretKey sessionKey, PrivateKey privateKey) {
        byte[] signedAesKey = sessionKey.getEncoded();
        return toByteArray(decrypt(signedAesKey, privateKey, SESSION_CIPHER_ALGORITHM));
    }

    public static boolean sessionKeyIsValid(SecretKey sessionKey, PrivateKey privateKey) {
        byte[] secret = decryptSessionKey(sessionKey, privateKey);
        Optional<SecretKey> sessionKeyClone = generateSessionKey(secret);
        if (sessionKeyClone.isPresent())
            return Arrays.equals(sessionKey.getEncoded(), sessionKeyClone.get().getEncoded());
        return false;
    }

    public static String encryptPacket(Packet p, Key key, SecretKey sessionKey) {
        String header = p.getHeader();
        String unencryptedContent = p.serialize();
        String hash = Packet.HASH_PREFIX + generateHash(unencryptedContent);
        byte[] encryptedHeaderAndContent = encrypt(header + "\n" + unencryptedContent + "\n" + hash, key);

        return encryptWithSessionKey(encryptedHeaderAndContent, sessionKey);
    }

    public static String decryptPacket(String encryptedPacket, Key key, SecretKey sessionKey) {
        byte[] sessionDecryptedContent = decryptWithSessionKey(encryptedPacket, sessionKey);
        return decrypt(sessionDecryptedContent, key);
    }

    public static Optional<PrivateKey> storePubAndGetKey(File out) {
        Optional<KeyPair> optionalKeyPair = generateKeyPair();
        if (optionalKeyPair.isPresent()) {
            KeyPair keyPair = optionalKeyPair.get();
            try {
                OutputStream stream;
                stream = new FileOutputStream(out);
                byte[] encoded = keyPair.getPublic().getEncoded();
                stream.write(encoded);
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Optional.ofNullable(keyPair.getPrivate());
        }
        return Optional.empty();
    }

    public static Optional<PublicKey> getPublicKeyFromFile(File file) {
        Path path = Paths.get(file.toURI());
        try {
            byte[] bytes = Files.readAllBytes(path);
            X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance(KEY_ALGORITHM);
            return Optional.ofNullable(kf.generatePublic(ks));
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
