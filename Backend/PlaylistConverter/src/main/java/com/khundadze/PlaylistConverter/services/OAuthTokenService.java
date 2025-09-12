package com.khundadze.PlaylistConverter.services;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.khundadze.PlaylistConverter.models_db.User;
import com.khundadze.PlaylistConverter.repo.UserRepository;
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

    private final CurrentUserProvider userProvider;
    private final UserRepository userRepository;

    @Transactional
    public OAuthTokenResponseDto save(StreamingPlatform service, String accessToken, String refreshToken,
                                      Instant expiry) {
        Long userId = userProvider.getId(); // This line causes the error in the callback
        return saveForUser(userId, service, accessToken, refreshToken, expiry);
    }

    @Transactional
    public OAuthTokenResponseDto saveForUser(Long userId, StreamingPlatform service, String accessToken, String refreshToken,
                                             Instant expiry) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Optional<OAuthToken> existing = tokenRepository.findByIdUserIdAndIdPlatform(userId, service);
        OAuthToken token;

        if (existing.isPresent()) {
            token = existing.get();
            token.setAccessToken(accessToken);
            if (refreshToken != null) {
                token.setRefreshToken(refreshToken);
            }
            token.setExpiresAt(expiry);
        } else {
            token = new OAuthToken();
            token.setId(new OAuthTokenId(userId, service));
            token.setUser(user);
            token.setAccessToken(accessToken);
            token.setRefreshToken(refreshToken);
            token.setExpiresAt(expiry);
        }
        return mapper.toOAuthTokenResponseDto(tokenRepository.save(token));
    }

    @Transactional(readOnly = true)
    public OAuthTokenResponseDto getValidAccessTokenDto(StreamingPlatform service) {
        Long userId = userProvider.getId();

        return tokenRepository.findByIdUserIdAndIdPlatform(userId, service)
                .filter(token -> token.getExpiresAt() == null || token.getExpiresAt().isAfter(Instant.now()))
                .map(mapper::toOAuthTokenResponseDto)
                .orElse(null);
    }

    @Transactional
    public void deleteOAuthTokenForUser(StreamingPlatform service) {
        Long userId = userProvider.getId();

        tokenRepository.findByIdUserIdAndIdPlatform(userId, service)
                .ifPresent(tokenRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<OAuthTokenResponseDto> getAllOAuthTokensForUser() {
        Long userId = userProvider.getId();
        return tokenRepository.findAllByUser_Id(userId).stream().map(mapper::toOAuthTokenResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public List<StreamingPlatform> getAuthenticatedPlatforms() {
        Long userId = userProvider.getId();

        return tokenRepository.findAllByUser_Id(userId).stream()
                .map(OAuthToken::getPlatform)
                .distinct()
                .toList();
    }

}
