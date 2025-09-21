package com.khundadze.PlaylistConverter.securityConfig.oauth2ForPlatformAuth;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StateManager { // TODO: replace with Redis

    private final Map<String, Object> stateToPrincipalId = new ConcurrentHashMap<>();

    private final Map<String, String> stateToCodeVerifier = new ConcurrentHashMap<>();

    /**
     * Generates a new, unique state string.
     */
    public String generateState() {
        return UUID.randomUUID().toString();
    }

    // --- Principal mapping ---

    /**
     * Associates a state string with a principal's ID (either a Long or a String).
     */
    public void putPrincipal(String state, Object principalId) {
        stateToPrincipalId.put(state, principalId);
    }

    /**
     * Retrieves and removes the principal's ID associated with a state string.
     */
    public Object removePrincipal(String state) {
        return stateToPrincipalId.remove(state);
    }

    // --- PKCE code verifier mapping (no changes needed) ---

    public void putCodeVerifier(String state, String codeVerifier) {
        stateToCodeVerifier.put(state, codeVerifier);
    }

    public String removeCodeVerifier(String state) {
        return stateToCodeVerifier.remove(state);
    }

    // REMOVED: The tempTokens map and TimedToken class are no longer needed.
}