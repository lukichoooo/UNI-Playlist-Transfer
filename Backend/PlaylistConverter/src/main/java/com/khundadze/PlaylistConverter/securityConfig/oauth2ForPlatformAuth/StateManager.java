package com.khundadze.PlaylistConverter.securityConfig.oauth2ForPlatformAuth;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class StateManager {

    private final StringRedisTemplate redisTemplate;
    private final long EXPIRATION_TIME_MINUTES = 5;

    public StateManager(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateState() {
        return UUID.randomUUID().toString();
    }

    // --- Principal mapping ---

    public void putPrincipal(String state, Object principalId, boolean isRegisteredUser) {
        String key = "principal:" + state;
        String value = (isRegisteredUser ? "user:" : "guest:") + principalId.toString();
        redisTemplate.opsForValue().set(key, value, EXPIRATION_TIME_MINUTES, TimeUnit.MINUTES);
    }

    public Object removePrincipal(String state) {
        String key = "principal:" + state;
        String principalId = redisTemplate.opsForValue().get(key);
        if (principalId != null) {
            redisTemplate.delete(key);
        }
        return principalId;
    }

    // --- PKCE code verifier mapping ---

    public void putCodeVerifier(String state, String codeVerifier) {
        String key = "codeVerifier:" + state;
        redisTemplate.opsForValue().set(key, codeVerifier, EXPIRATION_TIME_MINUTES, TimeUnit.MINUTES);
    }

    public String removeCodeVerifier(String state) {
        String key = "codeVerifier:" + state;
        String codeVerifier = redisTemplate.opsForValue().get(key);
        if (codeVerifier != null) {
            redisTemplate.delete(key);
        }
        return codeVerifier;
    }

    // --- OrincipalValue checks ---
    public boolean isRegisteredUser(String principalValue) {
        if (principalValue == null) {
            return false;
        }
        if (principalValue == null || (!principalValue.startsWith("user:") && !principalValue.startsWith("guest:"))) {
            throw new IllegalStateException("Unknown principal type stored in state manager: " + principalValue);
        }
        return principalValue.startsWith("user:");
    }
}