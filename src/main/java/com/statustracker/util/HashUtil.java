package com.statustracker.util;

import com.statustracker.model.StatusUpdate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class HashUtil {
    
    /**
     * Generate a hash of status update to detect duplicates
     */
    public static String generateHash(StatusUpdate update) {
        String input = update.getServiceId() + "|" + 
                      update.getStatus() + "|" + 
                      update.getStatusMessage();
        return sha256(input);
    }
    
    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encoded);
        } catch (Exception e) {
            return input.hashCode() + "";
        }
    }
    
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
