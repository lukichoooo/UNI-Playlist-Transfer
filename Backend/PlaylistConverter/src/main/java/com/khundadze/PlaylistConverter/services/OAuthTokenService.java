package com.khundadze.PlaylistConverter.services;

import com.khundadze.PlaylistConverter.dtos.OAuthTokenResponseDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.exceptions.UserNotFoundException;
import com.khundadze.PlaylistConverter.models_db.OAuthToken;
import com.khundadze.PlaylistConverter.models_db.OAuthTokenId;
import com.khundadze.PlaylistConverter.models_db.User;
import com.khundadze.PlaylistConverter.repo.OAuthTokenRepository;
import com.khundadze.PlaylistConverter.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthTokenService {

    private final OAuthTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final OAuthTokenMapper mapper;

    private final CurrentUserProvider userProvider;


    @Transactional
    public OAuthTokenResponseDto save(Long userId, StreamingPlatform service, String accessToken, String refreshToken,
                                      Instant expiry) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

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
        return mapper.decryptTokenDto(mapper.toOAuthTokenResponseDto(tokenRepository.save(mapper.encryptToken(token))));
    }


    @Transactional(readOnly = true)
    public OAuthTokenResponseDto getValidAccessTokenDto(StreamingPlatform service) {
        Long userId = userProvider.getId();

        return tokenRepository.findByIdUserIdAndIdPlatform(userId, service)
                .filter(token -> token.getExpiresAt() == null || token.getExpiresAt().isAfter(Instant.now()))
                .map(mapper::toOAuthTokenResponseDto)
                .map(mapper::decryptTokenDto)
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
        return tokenRepository.findAllByUser_Id(userId).stream()
                .map(mapper::toOAuthTokenResponseDto)
                .map(mapper::decryptTokenDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StreamingPlatform> getAuthenticatedPlatforms() {
        if (!userProvider.isLoggedIn()) {
            return Collections.emptyList(); // TODO: return from cache
        }
        Long userId = userProvider.getId();

        Instant now = Instant.now();

        return tokenRepository.findAllByUser_Id(userId).stream()
                .filter(token -> token.getExpiresAt() == null || token.getExpiresAt().isAfter(now))
                .map(OAuthToken::getPlatform)
                .distinct()
                .toList();
    }


}
