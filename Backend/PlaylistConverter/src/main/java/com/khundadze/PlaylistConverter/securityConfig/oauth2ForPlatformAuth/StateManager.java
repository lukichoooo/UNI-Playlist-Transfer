package com.khundadze.PlaylistConverter.securityConfig.oauth2ForPlatformAuth;

import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StateManager { // TODO: replace with Redis

    private final Map<String, Long> stateToUserId = new ConcurrentHashMap<>();
    private final Map<String, TimedToken> tempTokens = new ConcurrentHashMap<>();
    private final Map<String, String> stateToCodeVerifier = new ConcurrentHashMap<>();
    private final Map<String, String> transferState = new ConcurrentHashMap<>();
    private final long TEMP_TOKEN_TTL_SECONDS = 30 * 60; // 30 minutes

    // --- State generation ---
    public String generateState() {
        return UUID.randomUUID().toString();
    }

    // --- User mapping ---
    public void putUser(String state, Long userId) {
        stateToUserId.put(state, userId);
    }

    public Long removeUser(String state) {
        return stateToUserId.remove(state);
    }

    // --- Temporary token mapping ---
    public void putTempToken(String state, OAuth2AccessTokenResponse token) {
        tempTokens.put(state, new TimedToken(token, Instant.now().getEpochSecond()));
    }

    public OAuth2AccessTokenResponse removeTempToken(String state) {
        TimedToken timed = tempTokens.remove(state);
        if (timed == null) return null;

        // Check if token expired
        long now = Instant.now().getEpochSecond();
        if (now - timed.timestamp > TEMP_TOKEN_TTL_SECONDS) {
            return null; // expired
        }
        return timed.token;
    }

    // --- PKCE code verifier mapping ---
    public void putCodeVerifier(String state, String codeVerifier) {
        stateToCodeVerifier.put(state, codeVerifier);
    }

    public String removeCodeVerifier(String state) {
        return stateToCodeVerifier.remove(state);
    }

    // --- Internal helper class to track timestamp ---
    public static class TimedToken {
        OAuth2AccessTokenResponse token;
        long timestamp; // epoch seconds

        TimedToken(OAuth2AccessTokenResponse token, long timestamp) {
            this.token = token;
            this.timestamp = timestamp;
        }
    }
}
