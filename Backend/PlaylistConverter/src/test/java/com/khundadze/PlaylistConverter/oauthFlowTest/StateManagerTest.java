package com.khundadze.PlaylistConverter.securityConfig.oauth2ForPlatformAuth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StateManagerTest {

    private StateManager stateManager;

    @BeforeEach
    void setUp() {
        stateManager = new StateManager();
    }

    @Test
    void generateState() {
        String state1 = stateManager.generateState();
        String state2 = stateManager.generateState();

        assertNotNull(state1);
        assertFalse(state1.isEmpty());
        assertNotNull(state2);
        assertNotEquals(state1, state2);
    }

    @Test
    void putAndRemovePrincipal_LongId() {
        String state = stateManager.generateState();
        Long principalId = 123L;

        stateManager.putPrincipal(state, principalId);
        Object removedPrincipalId = stateManager.removePrincipal(state);

        assertEquals(principalId, removedPrincipalId);
        assertNull(stateManager.removePrincipal(state));
    }

    @Test
    void putAndRemovePrincipal_StringId() {
        String state = stateManager.generateState();
        String principalId = "user-abc-123";

        stateManager.putPrincipal(state, principalId);
        Object removedPrincipalId = stateManager.removePrincipal(state);

        assertEquals(principalId, removedPrincipalId);
        assertNull(stateManager.removePrincipal(state));
    }


    @Test
    void removePrincipal_nonExistentState() {
        assertNull(stateManager.removePrincipal("non-existent-state"));
    }

    @Test
    void putAndRemoveCodeVerifier() {
        String state = stateManager.generateState();
        String codeVerifier = "test_code_verifier";

        stateManager.putCodeVerifier(state, codeVerifier);
        String removedCodeVerifier = stateManager.removeCodeVerifier(state);

        assertEquals(codeVerifier, removedCodeVerifier);
        assertNull(stateManager.removeCodeVerifier(state));
    }

    @Test
    void removeCodeVerifier_nonExistentState() {
        assertNull(stateManager.removeCodeVerifier("non-existent-state"));
    }

    @Test
    void mapsShouldBehaveIndependently() {
        String state = stateManager.generateState();
        Long principalId = 456L;
        String codeVerifier = "independent_verifier";

        stateManager.putPrincipal(state, principalId);
        stateManager.putCodeVerifier(state, codeVerifier);

        assertEquals(principalId, stateManager.removePrincipal(state));
        assertEquals(codeVerifier, stateManager.removeCodeVerifier(state));

        assertNull(stateManager.removePrincipal(state));
        assertNull(stateManager.removeCodeVerifier(state));
    }
}
