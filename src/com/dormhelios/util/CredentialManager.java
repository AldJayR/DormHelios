package com.dormhelios.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility class for managing remembered login credentials.
 * Handles securely saving and loading credentials to/from disk.
 */
public class CredentialManager {
    
    private static final Logger LOGGER = Logger.getLogger(CredentialManager.class.getName());
    private static final String CREDENTIALS_DIRECTORY = System.getProperty("user.home") + File.separator + ".dormhelios";
    private static final String CREDENTIALS_FILE = CREDENTIALS_DIRECTORY + File.separator + "credentials.properties";
    private static final String KEY_FILE = CREDENTIALS_DIRECTORY + File.separator + "key.bin";
    
    private static final String EMAIL_PROPERTY = "rememberedEmail";
    private static final String PASSWORD_PROPERTY = "rememberedPassword";
    
    private static SecretKey secretKey;
    
    // Ensure directory exists on first use
    static {
        try {
            Path directoryPath = Paths.get(CREDENTIALS_DIRECTORY);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create credentials directory", e);
        }
    }
    
    /**
     * Saves user credentials if "Remember Me" is checked.
     * 
     * @param email The user's email
     * @param password The user's password (as char array)
     * @param rememberMe Whether to save the credentials
     */
    public static void saveCredentials(String email, char[] password, boolean rememberMe) {
        if (!rememberMe) {
            // If remember me is not checked, delete any saved credentials
            deleteCredentials();
            return;
        }
        
        try {
            // Create or load encryption key
            initializeKey();
            
            // Encrypt the password
            String encryptedPassword = encrypt(new String(password));
            
            // Save credentials to properties file
            Properties props = new Properties();
            props.setProperty(EMAIL_PROPERTY, email);
            props.setProperty(PASSWORD_PROPERTY, encryptedPassword);
            
            try (FileOutputStream out = new FileOutputStream(CREDENTIALS_FILE)) {
                props.store(out, "DormHelios Remembered Credentials");
                LOGGER.log(Level.INFO, "Credentials saved successfully");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to save credentials", e);
        }
    }
    
    /**
     * Loads saved credentials if they exist.
     * 
     * @return Object array with [email, password, rememberMe] or null if no credentials
     */
    public static Object[] loadCredentials() {
        File credFile = new File(CREDENTIALS_FILE);
        
        if (!credFile.exists()) {
            return null;
        }
        
        try {
            // Initialize key for decryption
            initializeKey();
            
            // Load properties file
            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream(CREDENTIALS_FILE)) {
                props.load(in);
            }
            
            String email = props.getProperty(EMAIL_PROPERTY);
            String encryptedPassword = props.getProperty(PASSWORD_PROPERTY);
            
            if (email != null && encryptedPassword != null) {
                // Decrypt password
                String password = decrypt(encryptedPassword);
                
                // Return email, password as char array, and true for rememberMe
                return new Object[] { email, password.toCharArray(), true };
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load credentials", e);
        }
        
        return null;
    }
    
    /**
     * Deletes any saved credentials.
     */
    public static void deleteCredentials() {
        try {
            File credFile = new File(CREDENTIALS_FILE);
            if (credFile.exists()) {
                Files.delete(credFile.toPath());
                LOGGER.log(Level.INFO, "Credentials deleted successfully");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete credentials", e);
        }
    }
    
    /**
     * Creates or loads the encryption key.
     */
    private static void initializeKey() throws Exception {
        File keyFile = new File(KEY_FILE);
        
        if (keyFile.exists()) {
            // Load existing key
            byte[] encodedKey = Files.readAllBytes(keyFile.toPath());
            secretKey = new SecretKeySpec(encodedKey, "AES");
        } else {
            // Create new key
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256); // Use 256-bit keys
            secretKey = keyGen.generateKey();
            
            // Save key
            try (FileOutputStream out = new FileOutputStream(keyFile)) {
                out.write(secretKey.getEncoded());
            }
        }
    }
    
    /**
     * Encrypts a string using AES encryption.
     * 
     * @param text The text to encrypt
     * @return Base64-encoded encrypted string
     */
    private static String encrypt(String text) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(text.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }
    
    /**
     * Decrypts a string using AES decryption.
     * 
     * @param encrypted The Base64-encoded encrypted string
     * @return Decrypted string
     */
    private static String decrypt(String encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decoded = Base64.getDecoder().decode(encrypted);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted);
    }
}