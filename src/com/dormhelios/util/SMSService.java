package com.dormhelios.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Utility class for sending SMS messages via the Semaphore.co SMS Gateway API.
 * This class handles the API communication, authentication, and error handling.
 */
public class SMSService {
    
    private static final Logger LOGGER = Logger.getLogger(SMSService.class.getName());
    private static final String API_BASE_URL = "https://api.semaphore.co/api/v4/messages";
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 2000; // 2 seconds
    
    private final String apiKey;
    private final String senderId;
    private boolean enabled;
    
    /**
     * Constructor with API key
     * 
     * @param apiKey The Semaphore API key
     */
    public SMSService(String apiKey) {
        this(apiKey, null);
    }
    
    /**
     * Constructor with API key and sender ID
     * 
     * @param apiKey The Semaphore API key
     * @param senderId The sender ID (optional)
     */
    public SMSService(String apiKey, String senderId) {
        this.apiKey = apiKey;
        this.senderId = senderId;
        this.enabled = (apiKey != null && !apiKey.isEmpty());
    }
    
    /**
     * Loads SMS service configuration from properties file
     * 
     * @return Configured SMSService instance
     */
    public static SMSService fromConfig() {
        try {
            Properties props = new Properties();
            props.load(SMSService.class.getResourceAsStream("/config.properties"));
            
            String apiKey = props.getProperty("sms.apiKey", "");
            String senderId = props.getProperty("sms.senderId", "");
            
            return new SMSService(apiKey, senderId.isEmpty() ? null : senderId);
        } catch (IOException | NullPointerException ex) {
            LOGGER.log(Level.WARNING, "Could not load SMS configuration. SMS service will be disabled.", ex);
            return new SMSService(null);
        }
    }
    
    /**
     * Checks if the SMS service is enabled (has valid API key)
     * 
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Enable or disable the SMS service
     * 
     * @param enabled true to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled && (apiKey != null && !apiKey.isEmpty());
    }
    
    /**
     * Sends an SMS message to a single recipient
     * 
     * @param phoneNumber The recipient's phone number
     * @param message The message content
     * @return SMSResult object containing the result of the operation
     */
    public SMSResult sendSMS(String phoneNumber, String message) {
        if (!isEnabled()) {
            LOGGER.warning("SMS service is disabled. Message not sent.");
            return new SMSResult(false, "SMS service is disabled", null);
        }
        
        validatePhoneNumber(phoneNumber);
        validateMessage(message);
        
        Map<String, Object> params = new HashMap<>();
        params.put("apikey", apiKey);
        params.put("number", formatPhoneNumber(phoneNumber));
        params.put("message", message);
        
        if (senderId != null && !senderId.isEmpty()) {
            params.put("sendername", senderId);
        }
        
        return sendRequest(params);
    }
    
    /**
     * Sends the same SMS message to multiple recipients
     * 
     * @param phoneNumbers Array of recipient phone numbers
     * @param message The message content
     * @return SMSResult object containing the result of the operation
     */
    public SMSResult sendBulkSMS(String[] phoneNumbers, String message) {
        if (!isEnabled()) {
            LOGGER.warning("SMS service is disabled. Bulk message not sent.");
            return new SMSResult(false, "SMS service is disabled", null);
        }
        
        if (phoneNumbers == null || phoneNumbers.length == 0) {
            throw new IllegalArgumentException("Phone numbers array cannot be empty");
        }
        
        validateMessage(message);
        
        // Format and validate all phone numbers
        StringBuilder formattedNumbers = new StringBuilder();
        for (int i = 0; i < phoneNumbers.length; i++) {
            validatePhoneNumber(phoneNumbers[i]);
            formattedNumbers.append(formatPhoneNumber(phoneNumbers[i]));
            if (i < phoneNumbers.length - 1) {
                formattedNumbers.append(",");
            }
        }
        
        Map<String, Object> params = new HashMap<>();
        params.put("apikey", apiKey);
        params.put("number", formattedNumbers.toString());
        params.put("message", message);
        
        if (senderId != null && !senderId.isEmpty()) {
            params.put("sendername", senderId);
        }
        
        return sendRequest(params);
    }
    
    /**
     * Sends the actual HTTP request to the SMS API
     * 
     * @param params Map of request parameters
     * @return SMSResult object containing the result of the operation
     */
    private SMSResult sendRequest(Map<String, Object> params) {
        int retries = 0;
        while (retries < MAX_RETRIES) {
            HttpURLConnection conn = null;
            try {
                // Create JSON payload
                JSONObject jsonPayload = new JSONObject(params);
                String requestBody = jsonPayload.toJSONString();
                LOGGER.fine("Sending SMS with payload: " + requestBody);
                
                // Setup connection
                URL url = new URL(API_BASE_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                
                // Send request
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                
                // Handle response
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || 
                    responseCode == HttpURLConnection.HTTP_CREATED) {
                    
                    // Read the response
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            response.append(line);
                        }
                        
                        // Parse response JSON
                        JSONParser parser = new JSONParser();
                        JSONObject jsonResponse = (JSONObject) parser.parse(response.toString());
                        String messageId = jsonResponse.containsKey("message_id") ? 
                                jsonResponse.get("message_id").toString() : null;
                        
                        LOGGER.info("SMS sent successfully. Message ID: " + messageId);
                        return new SMSResult(true, "Message sent successfully", messageId);
                    } catch (ParseException e) {
                        LOGGER.log(Level.WARNING, "Error parsing SMS API response", e);
                        return new SMSResult(false, "Error parsing API response: " + e.getMessage(), null);
                    }
                } else {
                    // Handle error response
                    StringBuilder errorResponse = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            errorResponse.append(line);
                        }
                    }
                    
                    String errorMessage = "API Error: HTTP " + responseCode + " - " + errorResponse.toString();
                    LOGGER.warning(errorMessage);
                    
                    // Check if we should retry
                    if (isRetryableError(responseCode) && retries < MAX_RETRIES - 1) {
                        retries++;
                        LOGGER.info("Retrying SMS send (attempt " + retries + " of " + MAX_RETRIES + ")");
                        try {
                            Thread.sleep(RETRY_DELAY_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            return new SMSResult(false, "SMS send interrupted during retry: " + ie.getMessage(), null);
                        }
                    } else {
                        return new SMSResult(false, errorMessage, null);
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error sending SMS", e);
                if (retries < MAX_RETRIES - 1) {
                    retries++;
                    LOGGER.info("Retrying SMS send (attempt " + retries + " of " + MAX_RETRIES + ")");
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return new SMSResult(false, "SMS send interrupted during retry: " + ie.getMessage(), null);
                    }
                } else {
                    return new SMSResult(false, "Error sending SMS: " + e.getMessage(), null);
                }
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
        
        return new SMSResult(false, "Failed to send SMS after " + MAX_RETRIES + " attempts", null);
    }
    
    /**
     * Determines if an HTTP error code should trigger a retry
     * 
     * @param httpCode HTTP response code
     * @return true if should retry, false otherwise
     */
    private boolean isRetryableError(int httpCode) {
        // Retry server errors and some specific client errors
        return httpCode >= 500 || httpCode == 429; // 429 is Too Many Requests
    }
    
    /**
     * Validates a phone number format
     * 
     * @param phoneNumber Phone number to validate
     * @throws IllegalArgumentException if phone number is invalid
     */
    private void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }
        
        // Remove all non-numeric characters for validation
        String digitsOnly = phoneNumber.replaceAll("[^0-9]", "");
        
        // Basic validation for Philippine numbers
        if (digitsOnly.length() < 10 || digitsOnly.length() > 13) {
            throw new IllegalArgumentException("Invalid phone number format: " + phoneNumber);
        }
    }
    
    /**
     * Formats a phone number to ensure it's in the correct format for the API
     * 
     * @param phoneNumber The raw phone number
     * @return Formatted phone number
     */
    private String formatPhoneNumber(String phoneNumber) {
        // Remove all non-numeric characters
        String digitsOnly = phoneNumber.replaceAll("[^0-9]", "");
        
        // Format for Philippine numbers
        // If it starts with 0, replace with 63
        if (digitsOnly.startsWith("0")) {
            digitsOnly = "63" + digitsOnly.substring(1);
        } 
        // If it doesn't start with 63, add it
        else if (!digitsOnly.startsWith("63") && digitsOnly.length() <= 10) {
            digitsOnly = "63" + digitsOnly;
        }
        
        return digitsOnly;
    }
    
    /**
     * Validates the message content
     * 
     * @param message Message content to validate
     * @throws IllegalArgumentException if message is invalid
     */
    private void validateMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }
        
        if (message.length() > 160) {
            LOGGER.warning("SMS message is longer than 160 characters. It may be split into multiple messages.");
        }
    }
    
    /**
     * Result class for SMS operations
     */
    public static class SMSResult {
        private final boolean success;
        private final String message;
        private final String messageId;
        
        public SMSResult(boolean success, String message, String messageId) {
            this.success = success;
            this.message = message;
            this.messageId = messageId;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getMessageId() {
            return messageId;
        }
        
        @Override
        public String toString() {
            return "SMSResult{" + "success=" + success + ", message=" + message + 
                    ", messageId=" + messageId + '}';
        }
    }
}