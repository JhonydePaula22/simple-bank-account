package com.wearewaes.simple_bank_account.domain.services;

import com.wearewaes.simple_bank_account.domain.model.exceptions.InternalErrorException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptionService {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
    private static String ENCRYPTION_KEY;

    public EncryptionService(String encryptionKey) {
        ENCRYPTION_KEY = encryptionKey;
    }

    private static SecretKeySpec getSecretKeySpec(String encryptionKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encryptionKey);
        return new SecretKeySpec(decodedKey, ALGORITHM);
    }

    public String encrypt(String data) {
        try {
            SecretKeySpec secretKey = getSecretKeySpec(ENCRYPTION_KEY);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedData = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            throw new InternalErrorException("Failed to perform this operation. Try again later.");
        }
    }

    public String decrypt(String encryptedData) {
        try {
            SecretKeySpec secretKey = getSecretKeySpec(ENCRYPTION_KEY);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedData = cipher.doFinal(decodedData);
            return new String(decryptedData);
        } catch (Exception e) {
            throw new InternalErrorException("Failed to perform this operation. Try again later.");
        }
    }
}