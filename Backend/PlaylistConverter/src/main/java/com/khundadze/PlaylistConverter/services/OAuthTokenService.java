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
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthTokenService {

    private final OAuthTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final GuestService guestService;
    private final OAuthTokenMapper mapper;

    private final CurrentUserProvider userProvider;

    @Transactional
    public OAuthTokenResponseDto saveForRegisteredUser(Long userId, StreamingPlatform service, String accessToken, String refreshToken, Instant expiry) {
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

    public OAuthTokenResponseDto saveForGuest(String guestId, StreamingPlatform service, String accessToken, String refreshToken, Instant expiry) {
        OAuthToken token = new OAuthToken();
        token.setPlatform(service);
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setExpiresAt(expiry);

        guestService.addAuthTokens(guestId, List.of(mapper.encryptToken(token)));
        return mapper.decryptTokenDto(mapper.toOAuthTokenResponseDto(token));
    }

    @Transactional(readOnly = true)
    public OAuthTokenResponseDto getValidAccessTokenDto(StreamingPlatform service) {

        Optional<OAuthToken> optionalToken = userProvider.isLoggedIn()
                ? findTokenForRegisteredUser(service)
                : findTokenForGuest(service);

        return optionalToken
                .filter(token -> token.getExpiresAt() == null || token.getExpiresAt().isAfter(Instant.now()))
                .map(mapper::toOAuthTokenResponseDto)
                .map(mapper::decryptTokenDto)
                .orElse(null);
    }


    @Transactional
    public void deleteOAuthTokenForUser(StreamingPlatform service) {
        if (userProvider.isLoggedIn()) {
            Long userId = userProvider.getRegisteredUserId();
            tokenRepository.findByIdUserIdAndIdPlatform(userId, service)
                    .ifPresent(tokenRepository::delete);
        } else {
            // ADD THIS BLOCK FOR GUESTS
            String guestId = userProvider.getGuestId();
            guestService.removeTokenForPlatform(guestId, service);
        }
    }


    @Transactional(readOnly = true)
    public List<StreamingPlatform> getAuthenticatedPlatforms() {
        // Step 1: Get the raw list of tokens from the correct source.
        List<OAuthToken> userTokens;
        if (userProvider.isLoggedIn()) {
            Long userId = userProvider.getRegisteredUserId();
            userTokens = tokenRepository.findAllByUser_Id(userId);
        } else {
            String guestId = userProvider.getGuestId();
            userTokens = guestService.getAuthTokens(guestId);
        }

        // Step 2: Apply the shared processing logic to the list.
        return userTokens.stream()
                .filter(token -> token.getExpiresAt() == null || token.getExpiresAt().isAfter(Instant.now()))
                .map(OAuthToken::getPlatform)
                .distinct()
                .toList();
    }

    // helper methods
    private Optional<OAuthToken> findTokenForRegisteredUser(StreamingPlatform service) {
        Long userId = userProvider.getRegisteredUserId();
        return tokenRepository.findByIdUserIdAndIdPlatform(userId, service);
    }

    private Optional<OAuthToken> findTokenForGuest(StreamingPlatform service) {
        String guestId = userProvider.getGuestId();
        return guestService.getAuthTokens(guestId).stream()
                .filter(token -> token.getPlatform() == service)
                .findFirst();
    }
}
