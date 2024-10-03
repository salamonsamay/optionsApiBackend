package com.example.marketData;

import java.security.SecureRandom;

public class ApiKeyGenerator {

    // Define the characters allowed in the API key
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int KEY_LENGTH = 32; // Desired length of the API key

    public static String generateApiKey() {
        SecureRandom random = new SecureRandom();
        StringBuilder apiKey = new StringBuilder(KEY_LENGTH);

        for (int i = 0; i < KEY_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            apiKey.append(CHARACTERS.charAt(index));
        }

        return apiKey.toString();
    }

    public static void main(String[] args) {
        String apiKey = generateApiKey();
        System.out.println("Generated API Key: " + apiKey);
    }
}
