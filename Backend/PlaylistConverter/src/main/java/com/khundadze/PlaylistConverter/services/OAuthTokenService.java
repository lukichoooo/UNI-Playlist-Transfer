package com.khundadze.PlaylistConverter.services;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.khundadze.PlaylistConverter.dtos.OAuthTokenResponseDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.models_db.OAuthToken;
import com.khundadze.PlaylistConverter.models_db.OAuthTokenId;
import com.khundadze.PlaylistConverter.repo.OAuthTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthTokenService {

    private final OAuthTokenRepository tokenRepository;
    private final OAuthTokenMapper mapper;

    @Transactional
    public OAuthTokenResponseDto save(Long userId, StreamingPlatform service, String accessToken, String refreshToken,
            Instant expiry) {
        Optional<OAuthToken> existing = tokenRepository.findByIdUserIdAndIdService(userId, service);
        OAuthToken token;
        if (existing.isPresent()) {
            token = existing.get();
            token.setAccessToken(accessToken);
            token.setRefreshToken(refreshToken);
            token.setExpiresAt(expiry);
        } else {
            token = new OAuthToken();
            token.setId(new OAuthTokenId(userId, service));
            token.setAccessToken(accessToken);
            token.setRefreshToken(refreshToken);
            token.setExpiresAt(expiry);
        }
        return mapper.toOAuthTokenResponseDto(tokenRepository.save(token));
    }

    @Transactional(readOnly = true)
    public OAuthTokenResponseDto getValidAccessTokenDto(Long userId, StreamingPlatform service) {
        return tokenRepository.findByIdUserIdAndIdService(userId, service)
                .filter(token -> token.getExpiresAt() == null || token.getExpiresAt().isAfter(Instant.now()))
                .map(mapper::toOAuthTokenResponseDto)
                .orElse(null);
    }

    @Transactional
    public void deleteOAuthTokenForUser(Long userId, StreamingPlatform service) {
        tokenRepository.findByIdUserIdAndIdService(userId, service)
                .ifPresent(tokenRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<OAuthTokenResponseDto> getAllOAuthTokensForUser(Long userId) {
        return tokenRepository.findAllByUser_Id(userId).stream()
                .map(mapper::toOAuthTokenResponseDto)
                .toList();
    }

}
