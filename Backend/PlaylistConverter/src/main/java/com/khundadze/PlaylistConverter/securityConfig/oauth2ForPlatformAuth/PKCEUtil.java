package com.khundadze.PlaylistConverter.securityConfig.oauth2ForPlatformAuth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

public class PKCEUtil {

    private static final int CODE_VERIFIER_LENGTH = 64;
    private static final String CODE_VERIFIER_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~";
    private static final Random secureRandom = new SecureRandom();

    /**
     * Generates a random PKCE code verifier (43-128 chars).
     */
    public static String generateCodeVerifier() {
        StringBuilder sb = new StringBuilder(CODE_VERIFIER_LENGTH);
        for (int i = 0; i < CODE_VERIFIER_LENGTH; i++) {
            sb.append(CODE_VERIFIER_CHARS.charAt(secureRandom.nextInt(CODE_VERIFIER_CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * Generates the code challenge using SHA-256 + Base64URL encoding.
     */
    public static String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PKCE code challenge", e);
        }
    }
}
