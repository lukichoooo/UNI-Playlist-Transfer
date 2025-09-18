package com.khundadze.PlaylistConverter.oauthTokenTest;

import com.khundadze.PlaylistConverter.dtos.OAuthTokenResponseDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.exceptions.UserNotFoundException;
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
import static org.mockito.Mockito.*;

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

        // DTO returned after encryption+save and decryption
        OAuthTokenResponseDto decryptedDto = new OAuthTokenResponseDto("newAccess", StreamingPlatform.SPOTIFY);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tokenRepository.findByIdUserIdAndIdPlatform(userId, StreamingPlatform.SPOTIFY))
                .thenReturn(Optional.of(existing));

        // Mock the encryption step
        when(mapper.encryptToken(existing)).thenReturn(existing);

        // Mock the save to return the same token
        when(tokenRepository.save(existing)).thenReturn(existing);

        // Mock the conversion to DTO
        when(mapper.toOAuthTokenResponseDto(existing)).thenReturn(decryptedDto);

        // Mock decryption step (so we get plain values in DTO)
        when(mapper.decryptTokenDto(decryptedDto)).thenReturn(decryptedDto);

        OAuthTokenResponseDto result = service.save(
                userId,
                StreamingPlatform.SPOTIFY,
                "newAccess",
                "newRefresh",
                expiry
        );

        assertNotNull(result);
        assertEquals("newAccess", result.accessToken());
        assertEquals(StreamingPlatform.SPOTIFY, result.service());
        assertEquals(expiry, existing.getExpiresAt());

        verify(tokenRepository).save(existing);
        verify(mapper).encryptToken(existing);
        verify(mapper).decryptTokenDto(decryptedDto);
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

        OAuthToken newToken = new OAuthToken();
        newToken.setId(new OAuthTokenId(userId, platform));
        newToken.setUser(user);
        newToken.setAccessToken(accessToken);
        newToken.setRefreshToken(refreshToken);
        newToken.setExpiresAt(expiry);

        OAuthTokenResponseDto responseDto = new OAuthTokenResponseDto(accessToken, platform);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tokenRepository.findByIdUserIdAndIdPlatform(userId, platform)).thenReturn(Optional.empty());

        // Mock encryptToken to return the same token
        when(mapper.encryptToken(any(OAuthToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock save to return the saved token
        when(tokenRepository.save(any(OAuthToken.class))).thenReturn(newToken);

        // Mock mapping and decryption
        when(mapper.toOAuthTokenResponseDto(newToken)).thenReturn(responseDto);
        when(mapper.decryptTokenDto(responseDto)).thenReturn(responseDto);

        // Act
        OAuthTokenResponseDto result = service.save(userId, platform, accessToken, refreshToken, expiry);

        // Assert
        assertNotNull(result);
        assertEquals(accessToken, result.accessToken());
        assertEquals(platform, result.service());

        ArgumentCaptor<OAuthToken> captor = ArgumentCaptor.forClass(OAuthToken.class);
        verify(tokenRepository).save(captor.capture());
        OAuthToken tokenSaved = captor.getValue();

        assertEquals(userId, tokenSaved.getUser().getId());
        assertEquals(platform, tokenSaved.getId().getPlatform());
        assertEquals(accessToken, tokenSaved.getAccessToken());
        assertEquals(refreshToken, tokenSaved.getRefreshToken());
        assertEquals(expiry, tokenSaved.getExpiresAt());

        // Verify mapper calls
        verify(mapper).encryptToken(tokenSaved);
        verify(mapper).decryptTokenDto(responseDto);
    }


    @Test
    void save_shouldThrowWhenUserNotFound() {
        Long userId = 99L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                service.save(userId, StreamingPlatform.SOUNDCLOUD, "a", "r", Instant.now()));

        // No mapper or repository save calls should happen
        verifyNoInteractions(mapper, tokenRepository);
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

        OAuthTokenResponseDto dto = new OAuthTokenResponseDto("valid", StreamingPlatform.SOUNDCLOUD);

        when(tokenRepository.findByIdUserIdAndIdPlatform(1L, StreamingPlatform.SOUNDCLOUD))
                .thenReturn(Optional.of(token));

        when(mapper.toOAuthTokenResponseDto(token)).thenReturn(dto);
        when(mapper.decryptTokenDto(dto)).thenReturn(dto); // mock decryption

        OAuthTokenResponseDto result = service.getValidAccessTokenDto(StreamingPlatform.SOUNDCLOUD);

        assertNotNull(result);
        assertEquals("valid", result.accessToken());
        assertEquals(StreamingPlatform.SOUNDCLOUD, result.service());

        verify(mapper).decryptTokenDto(dto);
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

        OAuthTokenResponseDto result = service.getValidAccessTokenDto(StreamingPlatform.SOUNDCLOUD);

        assertNull(result);

        // Verify that mapper methods are NOT called for expired token
        verifyNoInteractions(mapper);
    }


    @Test
    void deleteOAuthTokenForUser_shouldDeleteIfExists() {
        // Arrange
        when(userProvider.getId()).thenReturn(1L);

        OAuthTokenId id = new OAuthTokenId(1L, StreamingPlatform.SPOTIFY);
        OAuthToken token = OAuthToken.builder().id(id).build();

        when(tokenRepository.findByIdUserIdAndIdPlatform(1L, StreamingPlatform.SPOTIFY))
                .thenReturn(Optional.of(token));

        // Act
        service.deleteOAuthTokenForUser(StreamingPlatform.SPOTIFY);

        // Assert
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

        OAuthTokenResponseDto dto1 = new OAuthTokenResponseDto("a1", StreamingPlatform.SPOTIFY);
        OAuthTokenResponseDto dto2 = new OAuthTokenResponseDto("a2", StreamingPlatform.YOUTUBE);

        when(mapper.toOAuthTokenResponseDto(token1)).thenReturn(dto1);
        when(mapper.toOAuthTokenResponseDto(token2)).thenReturn(dto2);

        // Mock decryption for each DTO
        when(mapper.decryptTokenDto(dto1)).thenReturn(dto1);
        when(mapper.decryptTokenDto(dto2)).thenReturn(dto2);

        List<OAuthTokenResponseDto> result = service.getAllOAuthTokensForUser();

        assertEquals(2, result.size());
        assertEquals("a1", result.get(0).accessToken());
        assertEquals(StreamingPlatform.SPOTIFY, result.get(0).service());
        assertEquals("a2", result.get(1).accessToken());
        assertEquals(StreamingPlatform.YOUTUBE, result.get(1).service());

        verify(mapper).decryptTokenDto(dto1);
        verify(mapper).decryptTokenDto(dto2);
    }


    // Add this test for getAuthenticatedPlatforms when user is not logged in
    // TODO: should change when i add caching
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
        when(mapper.decryptTokenDto(any(OAuthTokenResponseDto.class))).thenReturn(null);

        assertNull(service.getValidAccessTokenDto(StreamingPlatform.SPOTIFY));
    }

    @Test
    void getAuthenticatedPlatforms_shouldReturnOnlyValidPlatforms() {
        when(userProvider.isLoggedIn()).thenReturn(true);
        when(userProvider.getId()).thenReturn(5L);

        Instant now = Instant.now();

        // Valid token
        OAuthTokenId id1 = new OAuthTokenId(5L, StreamingPlatform.SPOTIFY);
        OAuthToken token1 = OAuthToken.builder()
                .id(id1)
                .accessToken("a1")
                .expiresAt(now.plusSeconds(60)) // valid
                .build();

        // Expired token
        OAuthTokenId id2 = new OAuthTokenId(5L, StreamingPlatform.YOUTUBE);
        OAuthToken token2 = OAuthToken.builder()
                .id(id2)
                .accessToken("a2")
                .expiresAt(now.minusSeconds(60)) // expired
                .build();

        // Another valid token on the same platform as token1 (duplicate)
        OAuthTokenId id3 = new OAuthTokenId(5L, StreamingPlatform.SPOTIFY);
        OAuthToken token3 = OAuthToken.builder()
                .id(id3)
                .accessToken("a3")
                .expiresAt(null) // no expiry, valid
                .build();

        when(tokenRepository.findAllByUser_Id(5L))
                .thenReturn(List.of(token1, token2, token3));

        List<StreamingPlatform> result = service.getAuthenticatedPlatforms();

        assertEquals(1, result.size()); // only SPOTIFY is valid
        assertTrue(result.contains(StreamingPlatform.SPOTIFY));
        assertFalse(result.contains(StreamingPlatform.YOUTUBE));
    }

}