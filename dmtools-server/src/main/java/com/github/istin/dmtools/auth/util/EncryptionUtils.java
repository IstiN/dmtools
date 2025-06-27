package com.github.istin.dmtools.auth.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Utility class for encrypting and decrypting sensitive configuration values.
 */
@Component
public class EncryptionUtils {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 256;
    
    private final String encryptionKey;
    private final String salt;
    private final String iv;
    
    /**
     * Constructor with encryption parameters injected from application properties.
     * 
     * @param encryptionKey The encryption key
     * @param salt The salt for key derivation
     * @param iv The initialization vector
     */
    public EncryptionUtils(
            @Value("${dmtools.encryption.key:defaultEncryptionKeyThatShouldBeOverriddenInProduction}") String encryptionKey,
            @Value("${dmtools.encryption.salt:defaultSaltValueThatShouldBeOverriddenInProduction}") String salt,
            @Value("${dmtools.encryption.iv:defaultIvValueTh}") String iv) {
        this.encryptionKey = encryptionKey;
        this.salt = salt;
        this.iv = iv;
    }
    
    /**
     * Encrypts a value.
     * 
     * @param value The value to encrypt
     * @return The encrypted value as a Base64 string
     */
    public String encrypt(String value) {
        try {
            SecretKey key = generateSecretKey();
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
            
            byte[] encryptedBytes = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting value", e);
        }
    }
    
    /**
     * Decrypts a value.
     * 
     * @param encryptedValue The encrypted value as a Base64 string
     * @return The decrypted value
     */
    public String decrypt(String encryptedValue) {
        try {
            SecretKey key = generateSecretKey();
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
            
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedValue);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting value", e);
        }
    }
    
    /**
     * Generates a secret key from the encryption key and salt.
     * 
     * @return The generated secret key
     */
    private SecretKey generateSecretKey() {
        try {
            KeySpec keySpec = new PBEKeySpec(
                    encryptionKey.toCharArray(),
                    salt.getBytes(StandardCharsets.UTF_8),
                    ITERATION_COUNT,
                    KEY_LENGTH);
            
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
            byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new RuntimeException("Error generating secret key", e);
        }
    }
} 