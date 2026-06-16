package com.github.devfrogora.service.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64; // Uses standard Java 8+ Base64
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CryptoHelper {
    public static final String MY_CUSTOM_KEY = "MySecretPassphrase123!";
    private static final String ALGORITHM = "AES";

    /**
     * Encrypts plain text, then converts the result to a Base64 String.
     * Useful for generating the QR code string on your server/service side.
     */
    public static String encryptToBase64(String plainText, String customKey) throws Exception {
        SecretKeySpec keySpec = generateKey(customKey);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // Pure Java Base64 encoder (No Android dependencies)
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Decodes a Base64 String, then decrypts it back to plain text.
     */
    public static String decryptFromBase64(String base64CipherText, String customKey) throws Exception {
        SecretKeySpec keySpec = generateKey(customKey);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        // Pure Java Base64 decoder (No Android dependencies)
        byte[] decodedBytes = Base64.getDecoder().decode(base64CipherText);

        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    /**
     * Helper to safely convert any custom key string into a valid 128-bit AES key.
     */
    private static SecretKeySpec generateKey(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes(StandardCharsets.UTF_8);
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();

        // AES requires exactly 16, 24, or 32 bytes. We select the first 16 bytes.
        byte[] aesKey = new byte[16];
        System.arraycopy(key, 0, aesKey, 0, 16);

        return new SecretKeySpec(aesKey, ALGORITHM);
    }
}