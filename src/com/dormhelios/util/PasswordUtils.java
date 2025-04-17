package com.dormhelios.util;

import java.util.logging.Level;
import java.util.logging.Logger;

// IMPORTANT: This uses BCrypt. Ensure the jBCrypt library is added to your project.
// Add a dependency like jbcrypt: implementation 'org.mindrot:jbcrypt:0.4' (Gradle)
// For NetBeans Ant: Right-click Libraries -> Add JAR/Folder -> Select jbcrypt-0.4.jar
import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    private static final Logger LOGGER = Logger.getLogger(PasswordUtils.class.getName());

    // --- BCrypt Implementation (Recommended) ---
    private static final int BCRYPT_WORKLOAD = 12; // Adjust workload factor as needed (higher is slower but stronger)

    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to hash null or empty password.");
            return null; // Or throw an exception
        }
        // Use char array for better security if possible, but BCrypt.hashpw takes String
        char[] passwordChars = plainTextPassword.toCharArray();
        try {
            String hash = BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(BCRYPT_WORKLOAD));
            return hash;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error hashing password", e);
            return null; // Or throw
        } finally {
            clearPasswordArray(passwordChars); // Clear the char array even if BCrypt needs String
        }
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null || plainPassword.isEmpty() || hashedPassword.isEmpty()) {
            return false;
        }
        // Use char array for better security if possible, but BCrypt.checkpw takes String
        char[] passwordChars = plainPassword.toCharArray();
        try {
            boolean matches = BCrypt.checkpw(plainPassword, hashedPassword);
            return matches;
        } catch (IllegalArgumentException e) {
            // Log specifically if the hash format is invalid, but still return false
            LOGGER.log(Level.WARNING, "Password check failed due to invalid hash format or other argument issue.", e);
            return false;
        } catch (Exception e) {
            // Log unexpected errors during check, but typically return false for security
            LOGGER.log(Level.WARNING, "Error checking password hash comparison", e);
            return false;
        } finally {
            clearPasswordArray(passwordChars); // Clear the char array
        }
    }

    /**
     * Securely clears a character array containing sensitive data like passwords.
     * @param passwordArray The character array to clear.
     */
    public static void clearPasswordArray(char[] passwordArray) {
        if (passwordArray != null) {
            java.util.Arrays.fill(passwordArray, '\0'); // Overwrite with null characters
        }
    }
}