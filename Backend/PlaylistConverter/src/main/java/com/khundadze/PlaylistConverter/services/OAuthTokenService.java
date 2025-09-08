package com.khundadze.PlaylistConverter.services;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.khundadze.PlaylistConverter.enums.MusicService;
import com.khundadze.PlaylistConverter.models_db.OAuthToken;
import com.khundadze.PlaylistConverter.repo.OAuthTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthTokenService {

    private final OAuthTokenRepository tokenRepository;

    /**
     * Save or update OAuth token for a user & service.
     */
    @Transactional
    public OAuthToken save(Long userId, MusicService service, String accessToken, String refreshToken, Instant expiry) {
        Optional<OAuthToken> existing = tokenRepository.findByUserIdAndService(userId, service);
        OAuthToken token;
        if (existing.isPresent()) {
            token = existing.get();
            token.setAccessToken(accessToken);
            token.setRefreshToken(refreshToken);
            token.setExpiresAt(expiry);
        } else {
            token = new OAuthToken();
            token.setUserId(userId);
            token.setService(service);
            token.setAccessToken(accessToken);
            token.setRefreshToken(refreshToken);
            token.setExpiresAt(expiry);
        }
        return tokenRepository.save(token);
    }

    /**
     * Get a valid (not expired) access token for a user & service.
     * Returns null if token is missing or expired.
     */
    @Transactional(readOnly = true)
    public String getValidAccessToken(Long userId, MusicService service) {
        return tokenRepository.findByUserIdAndService(userId, service)
                .filter(token -> token.getExpiresAt() == null || token.getExpiresAt().isAfter(Instant.now()))
                .map(OAuthToken::getAccessToken)
                .orElse(null);
    }

    /**
     * Delete a user's OAuth token for a given service.
     */
    @Transactional
    public void deleteOAuthTokenForUser(Long userId, MusicService service) {
        tokenRepository.findByUserIdAndService(userId, service)
                .ifPresent(tokenRepository::delete);
    }

    /**
     * Get all OAuth tokens for a user.
     */
    @Transactional(readOnly = true)
    public List<OAuthToken> getAllOAuthTokensForUser(Long userId) {
        return tokenRepository.findAllByUser_Id(userId);
    }
}
