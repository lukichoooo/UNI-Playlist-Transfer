package com.khundadze.PlaylistConverter.oauthTokenTest;

import com.khundadze.PlaylistConverter.dtos.OAuthTokenResponseDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.models_db.OAuthToken;
import com.khundadze.PlaylistConverter.models_db.OAuthTokenId;
import com.khundadze.PlaylistConverter.models_db.User;
import com.khundadze.PlaylistConverter.repo.OAuthTokenRepository;
import com.khundadze.PlaylistConverter.repo.UserRepository;
import com.khundadze.PlaylistConverter.services.CurrentUserProvider;
import com.khundadze.PlaylistConverter.services.OAuthTokenMapper;
import com.khundadze.PlaylistConverter.services.OAuthTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OAuthTokenServiceTest {

    @Mock
    private OAuthTokenRepository tokenRepository;

    @Mock
    private OAuthTokenMapper mapper;

    @Mock
    private CurrentUserProvider userProvider;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OAuthTokenService service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save_shouldUpdateExistingToken() {
        Long userId = 1L;
        Instant expiry = Instant.now().plusSeconds(120);
        OAuthTokenId id = new OAuthTokenId(userId, StreamingPlatform.SPOTIFY);

        User user = User.builder().id(userId).build();

        OAuthToken existing = OAuthToken.builder()
                .id(id)
                .user(user)
                .accessToken("oldAccess")
                .refreshToken("oldRefresh")
                .expiresAt(Instant.now())
                .build();

        OAuthTokenResponseDto dto = new OAuthTokenResponseDto("newAccess", StreamingPlatform.SPOTIFY);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tokenRepository.findByIdUserIdAndIdPlatform(userId, StreamingPlatform.SPOTIFY))
                .thenReturn(Optional.of(existing));
        when(tokenRepository.save(existing)).thenReturn(existing);
        when(mapper.toOAuthTokenResponseDto(existing)).thenReturn(dto);

        OAuthTokenResponseDto result = service.save(userId, StreamingPlatform.SPOTIFY, "newAccess", "newRefresh", expiry);

        assertNotNull(result);
        assertEquals("newAccess", result.accessToken());
        assertEquals(StreamingPlatform.SPOTIFY, result.service());
        assertEquals(expiry, existing.getExpiresAt());

        verify(tokenRepository).save(existing);
    }

    @Test
    void save_shouldCreateNewTokenWhenNotExists() {
        // Arrange
        Long userId = 1L;
        StreamingPlatform platform = StreamingPlatform.SPOTIFY;
        String accessToken = "access123";
        String refreshToken = "refresh123";
        Instant expiry = Instant.now().plusSeconds(3600);

        User user = new User();
        user.setId(userId);

        OAuthToken savedToken = new OAuthToken();
        savedToken.setId(new OAuthTokenId(userId, platform));
        savedToken.setUser(user);
        savedToken.setAccessToken(accessToken);
        savedToken.setRefreshToken(refreshToken);
        savedToken.setExpiresAt(expiry);

        OAuthTokenResponseDto responseDto = new OAuthTokenResponseDto(accessToken, platform);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tokenRepository.findByIdUserIdAndIdPlatform(userId, platform)).thenReturn(Optional.empty());
        when(tokenRepository.save(any(OAuthToken.class))).thenReturn(savedToken);
        when(mapper.toOAuthTokenResponseDto(savedToken)).thenReturn(responseDto);

        // Act
        OAuthTokenResponseDto result = service.save(userId, platform, accessToken, refreshToken, expiry);

        // Assert
        assertNotNull(result);
        assertEquals(accessToken, result.accessToken());

        ArgumentCaptor<OAuthToken> captor = ArgumentCaptor.forClass(OAuthToken.class);
        verify(tokenRepository).save(captor.capture());
        OAuthToken tokenSaved = captor.getValue();
        assertEquals(userId, tokenSaved.getUser().getId());
        assertEquals(platform, tokenSaved.getId().getPlatform());
        assertEquals(accessToken, tokenSaved.getAccessToken());
        assertEquals(refreshToken, tokenSaved.getRefreshToken());
        assertEquals(expiry, tokenSaved.getExpiresAt());
    }


    @Test
    void save_shouldThrowWhenUserNotFound() {
        Long userId = 99L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                service.save(userId, StreamingPlatform.SOUNDCLOUD, "a", "r", Instant.now()));
    }

    @Test
    void getValidAccessTokenDto_shouldReturnDtoIfValid() {
        when(userProvider.getId()).thenReturn(1L);

        OAuthTokenId id = new OAuthTokenId(1L, StreamingPlatform.SOUNDCLOUD);
        OAuthToken token = OAuthToken.builder()
                .id(id)
                .accessToken("valid")
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        when(tokenRepository.findByIdUserIdAndIdPlatform(1L, StreamingPlatform.SOUNDCLOUD))
                .thenReturn(Optional.of(token));
        when(mapper.toOAuthTokenResponseDto(token))
                .thenReturn(new OAuthTokenResponseDto("valid", StreamingPlatform.SOUNDCLOUD));

        OAuthTokenResponseDto result = service.getValidAccessTokenDto(StreamingPlatform.SOUNDCLOUD);

        assertNotNull(result);
        assertEquals("valid", result.accessToken());
        assertEquals(StreamingPlatform.SOUNDCLOUD, result.service());
    }

    @Test
    void getValidAccessTokenDto_shouldReturnNullIfExpired() {
        when(userProvider.getId()).thenReturn(1L);

        OAuthTokenId id = new OAuthTokenId(1L, StreamingPlatform.SOUNDCLOUD);
        OAuthToken token = OAuthToken.builder()
                .id(id)
                .accessToken("expired")
                .expiresAt(Instant.now().minusSeconds(10))
                .build();

        when(tokenRepository.findByIdUserIdAndIdPlatform(1L, StreamingPlatform.SOUNDCLOUD))
                .thenReturn(Optional.of(token));

        assertNull(service.getValidAccessTokenDto(StreamingPlatform.SOUNDCLOUD));
    }

    @Test
    void deleteOAuthTokenForUser_shouldDeleteIfExists() {
        when(userProvider.getId()).thenReturn(1L);

        OAuthTokenId id = new OAuthTokenId(1L, StreamingPlatform.SPOTIFY);
        OAuthToken token = OAuthToken.builder().id(id).build();

        when(tokenRepository.findByIdUserIdAndIdPlatform(1L, StreamingPlatform.SPOTIFY))
                .thenReturn(Optional.of(token));

        service.deleteOAuthTokenForUser(StreamingPlatform.SPOTIFY);

        verify(tokenRepository).delete(token);
    }

    @Test
    void getAllOAuthTokensForUser_shouldReturnDtos() {
        when(userProvider.getId()).thenReturn(5L);

        OAuthTokenId id1 = new OAuthTokenId(5L, StreamingPlatform.SPOTIFY);
        OAuthToken token1 = OAuthToken.builder().id(id1).accessToken("a1").build();

        OAuthTokenId id2 = new OAuthTokenId(5L, StreamingPlatform.YOUTUBE);
        OAuthToken token2 = OAuthToken.builder().id(id2).accessToken("a2").build();

        when(tokenRepository.findAllByUser_Id(5L)).thenReturn(List.of(token1, token2));

        when(mapper.toOAuthTokenResponseDto(token1))
                .thenReturn(new OAuthTokenResponseDto("a1", StreamingPlatform.SPOTIFY));
        when(mapper.toOAuthTokenResponseDto(token2))
                .thenReturn(new OAuthTokenResponseDto("a2", StreamingPlatform.YOUTUBE));

        List<OAuthTokenResponseDto> result = service.getAllOAuthTokensForUser();

        assertEquals(2, result.size());
        assertEquals("a1", result.get(0).accessToken());
        assertEquals(StreamingPlatform.SPOTIFY, result.get(0).service());
        assertEquals("a2", result.get(1).accessToken());
        assertEquals(StreamingPlatform.YOUTUBE, result.get(1).service());
    }

    // Add this test for getAuthenticatedPlatforms when user is not logged in
    @Test
    void getAuthenticatedPlatforms_shouldReturnEmptyListWhenUserNotLoggedIn() {
        when(userProvider.isLoggedIn()).thenReturn(false);

        List<StreamingPlatform> result = service.getAuthenticatedPlatforms();

        assertTrue(result.isEmpty());
    }

    // Add this test for delete when token doesn't exist
    @Test
    void deleteOAuthTokenForUser_shouldNotThrowWhenTokenNotFound() {
        when(userProvider.getId()).thenReturn(1L);
        when(tokenRepository.findByIdUserIdAndIdPlatform(1L, StreamingPlatform.SPOTIFY))
                .thenReturn(Optional.empty());

        // Should not throw exception
        assertDoesNotThrow(() -> service.deleteOAuthTokenForUser(StreamingPlatform.SPOTIFY));
    }

    // Add this test for getValidAccessTokenDto when token doesn't exist
    @Test
    void getValidAccessTokenDto_shouldReturnNullWhenTokenNotFound() {
        when(userProvider.getId()).thenReturn(1L);
        when(tokenRepository.findByIdUserIdAndIdPlatform(1L, StreamingPlatform.SPOTIFY))
                .thenReturn(Optional.empty());

        assertNull(service.getValidAccessTokenDto(StreamingPlatform.SPOTIFY));
    }

    // Simplify the getAuthenticatedPlatforms test
    @Test
    void getAuthenticatedPlatforms_shouldReturnDistinctPlatforms() {
        when(userProvider.isLoggedIn()).thenReturn(true);
        when(userProvider.getId()).thenReturn(5L);

        OAuthTokenId id1 = new OAuthTokenId(5L, StreamingPlatform.SPOTIFY);
        OAuthToken token1 = OAuthToken.builder().id(id1).accessToken("a1").build();

        OAuthTokenId id2 = new OAuthTokenId(5L, StreamingPlatform.YOUTUBE);
        OAuthToken token2 = OAuthToken.builder().id(id2).accessToken("a2").build();

        // Only need 2 tokens with different platforms
        when(tokenRepository.findAllByUser_Id(5L)).thenReturn(List.of(token1, token2));

        List<StreamingPlatform> result = service.getAuthenticatedPlatforms();

        assertEquals(2, result.size());
        assertTrue(result.contains(StreamingPlatform.SPOTIFY));
        assertTrue(result.contains(StreamingPlatform.YOUTUBE));
    }
}
