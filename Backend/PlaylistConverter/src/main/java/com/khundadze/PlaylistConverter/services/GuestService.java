package com.khundadze.PlaylistConverter.services;

import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.models_db.OAuthToken;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GuestService {
    private final Map<String, List<OAuthToken>> guestIdToAuthTokens = new ConcurrentHashMap<>();


    public void addAuthTokens(String guestId, List<OAuthToken> newTokens) {
        // The .merge() method is atomic and perfect for this.
        guestIdToAuthTokens.merge(
                guestId,
                new ArrayList<>(newTokens), // The initial value if the guest is new
                (existingTokens, tokensToAdd) -> {
                    return Stream.concat(existingTokens.stream(), tokensToAdd.stream())
                            .collect(Collectors.toList());
                }
        );
    }


    public List<OAuthToken> getAuthTokens(String guestId) {
        // Use getOrDefault to avoid returning null and prevent NullPointerExceptions.
        return guestIdToAuthTokens.getOrDefault(guestId, Collections.emptyList());
    }

    public void initializeGuest(String guestId) {
        guestIdToAuthTokens.putIfAbsent(guestId, new ArrayList<>());
    }

    public void clearGuestSession(String guestId) {
        guestIdToAuthTokens.remove(guestId);
    }

    public void removeTokenForPlatform(String guestId, StreamingPlatform platform) {
        guestIdToAuthTokens.computeIfPresent(guestId, (key, tokens) -> {
            List<OAuthToken> updatedTokens = tokens.stream()
                    .filter(token -> token.getPlatform() != platform)
                    .collect(Collectors.toList());
            return updatedTokens;
        });
    }
}