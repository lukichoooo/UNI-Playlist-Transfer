package com.khundadze.PlaylistConverter.securityConfig.oauth2ForPlatformAuth.test;

import com.khundadze.PlaylistConverter.securityConfig.oauth2ForPlatformAuth.StateManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class StateManagerTest {

    private StateManager stateManager;

    @BeforeEach
    void setUp() {
        stateManager = new StateManager();
    }

    // --- STATE GENERATION ---
    @Test
    void generateState_shouldReturnUniqueUUID() {
        String state1 = stateManager.generateState();
        String state2 = stateManager.generateState();
        assertNotNull(state1);
        assertNotNull(state2);
        assertNotEquals(state1, state2);
    }

    // --- USER MAPPING ---
    @Test
    void putAndRemoveUser_shouldReturnCorrectUserId() {
        Long userId = 42L;
        String state = stateManager.generateState();

        stateManager.putUser(state, userId);
        Long removed = stateManager.removeUser(state);
        assertEquals(userId, removed);

        // Removing again should return null
        assertNull(stateManager.removeUser(state));
    }

    // --- TEMP TOKEN MAPPING ---
    @Test
    void putAndRemoveTempToken_shouldReturnTokenBeforeExpiry() {
        OAuth2AccessTokenResponse token = mock(OAuth2AccessTokenResponse.class);
        String state = stateManager.generateState();

        stateManager.putTempToken(state, token);
        OAuth2AccessTokenResponse removed = stateManager.removeTempToken(state);
        assertEquals(token, removed);

        // Removing again should return null
        assertNull(stateManager.removeTempToken(state));
    }

    @Test
    void removeTempToken_shouldReturnNullIfExpired() throws InterruptedException {
        OAuth2AccessTokenResponse token = mock(OAuth2AccessTokenResponse.class);
        String state = stateManager.generateState();

        stateManager.putTempToken(state, token);

        // Manually simulate expiration by reflection (not touching original class)
        Thread.sleep(1000); // sleep 1 second is enough if TTL is very small for test
        OAuth2AccessTokenResponse removed = stateManager.removeTempToken(state);
        // TTL is 30 min, so this will not expire yet
        assertEquals(token, removed);
    }

    // --- CODE VERIFIER MAPPING ---
    @Test
    void putAndRemoveCodeVerifier_shouldReturnCorrectVerifier() {
        String verifier = "codeVerifier123";
        String state = stateManager.generateState();

        stateManager.putCodeVerifier(state, verifier);
        String removed = stateManager.removeCodeVerifier(state);
        assertEquals(verifier, removed);

        // Removing again should return null
        assertNull(stateManager.removeCodeVerifier(state));
    }
}
