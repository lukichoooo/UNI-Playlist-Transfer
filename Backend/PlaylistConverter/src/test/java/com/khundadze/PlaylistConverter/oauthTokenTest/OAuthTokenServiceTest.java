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
import com.khundadze.PlaylistConverter.services.GuestService;
import com.khundadze.PlaylistConverter.services.OAuthTokenMapper;
import com.khundadze.PlaylistConverter.services.OAuthTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuthTokenServiceTest {

    @Mock
    private OAuthTokenRepository tokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GuestService guestService;
    @Mock
    private OAuthTokenMapper mapper;
    @Mock
    private CurrentUserProvider userProvider;

    @InjectMocks
    private OAuthTokenService oauthTokenService;

    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_GUEST_ID = "guest-123";
    private static final StreamingPlatform TEST_PLATFORM = StreamingPlatform.SPOTIFY;
    private static final String FAKE_ACCESS_TOKEN = "fake_access_token";
    private static final String FAKE_REFRESH_TOKEN = "fake_refresh_token";

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(TEST_USER_ID).username("testuser").build();
    }

    @Test
    void saveForRegisteredUser_updatesExistingToken() {
        // Given
        OAuthToken existingToken = OAuthToken.builder()
                .id(new OAuthTokenId(TEST_USER_ID, TEST_PLATFORM))
                .user(testUser)
                .accessToken("old_token")
                .refreshToken("old_refresh")
                .expiresAt(Instant.now().minusSeconds(3600))
                .build();
        OAuthToken updatedToken = OAuthToken.builder()
                .id(new OAuthTokenId(TEST_USER_ID, TEST_PLATFORM))
                .user(testUser)
                .accessToken(FAKE_ACCESS_TOKEN)
                .refreshToken(FAKE_REFRESH_TOKEN)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        OAuthTokenResponseDto expectedDto = new OAuthTokenResponseDto(FAKE_ACCESS_TOKEN, TEST_PLATFORM);

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(tokenRepository.findByIdUserIdAndIdPlatform(TEST_USER_ID, TEST_PLATFORM)).thenReturn(Optional.of(existingToken));
        when(mapper.encryptToken(any(OAuthToken.class))).thenReturn(updatedToken);
        when(tokenRepository.save(updatedToken)).thenReturn(updatedToken);
        when(mapper.toOAuthTokenResponseDto(updatedToken)).thenReturn(expectedDto);
        when(mapper.decryptTokenDto(expectedDto)).thenReturn(expectedDto);

        // When
        OAuthTokenResponseDto result = oauthTokenService.saveForRegisteredUser(TEST_USER_ID, TEST_PLATFORM, FAKE_ACCESS_TOKEN, FAKE_REFRESH_TOKEN, Instant.now().plusSeconds(3600));

        // Then
        assertEquals(expectedDto, result);
        verify(tokenRepository).findByIdUserIdAndIdPlatform(TEST_USER_ID, TEST_PLATFORM);
        verify(tokenRepository).save(updatedToken);
    }

    @Test
    void saveForRegisteredUser_createNewTokenIfNotFound() {
        // Given
        OAuthToken newToken = new OAuthToken();
        OAuthToken encryptedToken = new OAuthToken();
        OAuthTokenResponseDto expectedDto = new OAuthTokenResponseDto(FAKE_ACCESS_TOKEN, TEST_PLATFORM);

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(tokenRepository.findByIdUserIdAndIdPlatform(TEST_USER_ID, TEST_PLATFORM)).thenReturn(Optional.empty());
        when(mapper.encryptToken(any(OAuthToken.class))).thenReturn(encryptedToken);
        when(tokenRepository.save(encryptedToken)).thenReturn(encryptedToken);
        when(mapper.toOAuthTokenResponseDto(encryptedToken)).thenReturn(expectedDto);
        when(mapper.decryptTokenDto(expectedDto)).thenReturn(expectedDto);

        // When
        OAuthTokenResponseDto result = oauthTokenService.saveForRegisteredUser(TEST_USER_ID, TEST_PLATFORM, FAKE_ACCESS_TOKEN, FAKE_REFRESH_TOKEN, Instant.now().plusSeconds(3600));

        // Then
        assertNotNull(result);
        assertEquals(TEST_PLATFORM, result.service());
        assertEquals(FAKE_ACCESS_TOKEN, result.accessToken());
        verify(tokenRepository).findByIdUserIdAndIdPlatform(TEST_USER_ID, TEST_PLATFORM);
        verify(tokenRepository).save(any(OAuthToken.class));
    }

    @Test
    void saveForRegisteredUser_throwsExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(UserNotFoundException.class, () ->
                oauthTokenService.saveForRegisteredUser(TEST_USER_ID, TEST_PLATFORM, FAKE_ACCESS_TOKEN, FAKE_REFRESH_TOKEN, Instant.now())
        );
        verify(userRepository).findById(TEST_USER_ID);
        verifyNoInteractions(tokenRepository);
    }

    @Test
    void saveForGuest_savesToken() {
        // Given
        OAuthToken encryptedToken = new OAuthToken();
        OAuthTokenResponseDto expectedDto = new OAuthTokenResponseDto(FAKE_ACCESS_TOKEN, TEST_PLATFORM);

        when(mapper.encryptToken(any(OAuthToken.class))).thenReturn(encryptedToken);
        when(mapper.toOAuthTokenResponseDto(any(OAuthToken.class))).thenReturn(expectedDto);
        when(mapper.decryptTokenDto(expectedDto)).thenReturn(expectedDto);

        // When
        OAuthTokenResponseDto result = oauthTokenService.saveForGuest(TEST_GUEST_ID, TEST_PLATFORM, FAKE_ACCESS_TOKEN, FAKE_REFRESH_TOKEN, Instant.now());

        // Then
        assertNotNull(result);
        assertEquals(TEST_PLATFORM, result.service());
        assertEquals(FAKE_ACCESS_TOKEN, result.accessToken());
        verify(guestService).addAuthTokens(eq(TEST_GUEST_ID), anyList());
    }

    @Test
    void getValidAccessTokenDto_returnsValidTokenForRegisteredUser() {
        // Given
        OAuthToken validToken = OAuthToken.builder()
                .id(new OAuthTokenId(TEST_USER_ID, TEST_PLATFORM))
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        when(userProvider.isLoggedIn()).thenReturn(true);
        when(userProvider.getRegisteredUserId()).thenReturn(TEST_USER_ID);
        when(tokenRepository.findByIdUserIdAndIdPlatform(TEST_USER_ID, TEST_PLATFORM)).thenReturn(Optional.of(validToken));
        when(mapper.toOAuthTokenResponseDto(validToken)).thenReturn(new OAuthTokenResponseDto(FAKE_ACCESS_TOKEN, TEST_PLATFORM));
        when(mapper.decryptTokenDto(any(OAuthTokenResponseDto.class))).thenReturn(new OAuthTokenResponseDto(FAKE_ACCESS_TOKEN, TEST_PLATFORM));

        // When
        OAuthTokenResponseDto result = oauthTokenService.getValidAccessTokenDto(TEST_PLATFORM);

        // Then
        assertNotNull(result);
        assertEquals(FAKE_ACCESS_TOKEN, result.accessToken());
    }

    @Test
    void getValidAccessTokenDto_returnsNullForExpiredTokenForRegisteredUser() {
        // Given
        OAuthToken expiredToken = OAuthToken.builder()
                .id(new OAuthTokenId(TEST_USER_ID, TEST_PLATFORM))
                .expiresAt(Instant.now().minusSeconds(3600))
                .build();
        when(userProvider.isLoggedIn()).thenReturn(true);
        when(userProvider.getRegisteredUserId()).thenReturn(TEST_USER_ID);
        when(tokenRepository.findByIdUserIdAndIdPlatform(TEST_USER_ID, TEST_PLATFORM)).thenReturn(Optional.of(expiredToken));

        // When
        OAuthTokenResponseDto result = oauthTokenService.getValidAccessTokenDto(TEST_PLATFORM);

        // Then
        assertNull(result);
    }

    @Test
    void getValidAccessTokenDto_returnsValidTokenForGuest() {
        // Given
        OAuthToken validToken = OAuthToken.builder()
                .id(new OAuthTokenId(null, TEST_PLATFORM))
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        when(userProvider.isLoggedIn()).thenReturn(false);
        when(userProvider.getGuestId()).thenReturn(TEST_GUEST_ID);
        when(guestService.getAuthTokens(TEST_GUEST_ID)).thenReturn(List.of(validToken));
        when(mapper.toOAuthTokenResponseDto(validToken)).thenReturn(new OAuthTokenResponseDto(FAKE_ACCESS_TOKEN, TEST_PLATFORM));
        when(mapper.decryptTokenDto(any(OAuthTokenResponseDto.class))).thenReturn(new OAuthTokenResponseDto(FAKE_ACCESS_TOKEN, TEST_PLATFORM));

        // When
        OAuthTokenResponseDto result = oauthTokenService.getValidAccessTokenDto(TEST_PLATFORM);

        // Then
        assertNotNull(result);
        assertEquals(FAKE_ACCESS_TOKEN, result.accessToken());
    }

    @Test
    void deleteOAuthTokenForUser_deletesForRegisteredUser() {
        // Given
        when(userProvider.isLoggedIn()).thenReturn(true);
        when(userProvider.getRegisteredUserId()).thenReturn(TEST_USER_ID);
        OAuthToken token = new OAuthToken();
        when(tokenRepository.findByIdUserIdAndIdPlatform(TEST_USER_ID, TEST_PLATFORM)).thenReturn(Optional.of(token));

        // When
        oauthTokenService.deleteOAuthTokenForUser(TEST_PLATFORM);

        // Then
        verify(tokenRepository).delete(token);
    }

    @Test
    void deleteOAuthTokenForUser_removesForGuest() {
        // Given
        when(userProvider.isLoggedIn()).thenReturn(false);
        when(userProvider.getGuestId()).thenReturn(TEST_GUEST_ID);

        // When
        oauthTokenService.deleteOAuthTokenForUser(TEST_PLATFORM);

        // Then
        verify(guestService).removeTokenForPlatform(TEST_GUEST_ID, TEST_PLATFORM);
    }

    @Test
    void getAuthenticatedPlatforms_returnsListOfPlatformsForRegisteredUser() {
        // Given
        OAuthToken token1 = OAuthToken.builder().id(new OAuthTokenId(TEST_USER_ID, StreamingPlatform.SPOTIFY)).expiresAt(Instant.now().plusSeconds(3600)).build();
        OAuthToken token2 = OAuthToken.builder().id(new OAuthTokenId(TEST_USER_ID, StreamingPlatform.YOUTUBE)).expiresAt(Instant.now().minusSeconds(3600)).build();
        when(userProvider.isLoggedIn()).thenReturn(true);
        when(userProvider.getRegisteredUserId()).thenReturn(TEST_USER_ID);
        when(tokenRepository.findAllByUser_Id(TEST_USER_ID)).thenReturn(List.of(token1, token2));

        // When
        List<StreamingPlatform> result = oauthTokenService.getAuthenticatedPlatforms();

        // Then
        assertEquals(1, result.size());
        assertTrue(result.contains(StreamingPlatform.SPOTIFY));
    }

    @Test
    void getAuthenticatedPlatforms_returnsListOfPlatformsForGuest() {
        // Given
        OAuthToken token1 = OAuthToken.builder().id(new OAuthTokenId(null, StreamingPlatform.SOUNDCLOUD)).expiresAt(Instant.now().plusSeconds(3600)).build();
        OAuthToken token2 = OAuthToken.builder().id(new OAuthTokenId(null, StreamingPlatform.DEEZER)).expiresAt(Instant.now().plusSeconds(3600)).build();
        when(userProvider.isLoggedIn()).thenReturn(false);
        when(userProvider.getGuestId()).thenReturn(TEST_GUEST_ID);
        when(guestService.getAuthTokens(TEST_GUEST_ID)).thenReturn(List.of(token1, token2));

        // When
        List<StreamingPlatform> result = oauthTokenService.getAuthenticatedPlatforms();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(StreamingPlatform.SOUNDCLOUD));
        assertTrue(result.contains(StreamingPlatform.DEEZER));
    }

    @Test
    void getAuthenticatedPlatforms_returnsEmptyListIfNoTokens() {
        // Given
        when(userProvider.isLoggedIn()).thenReturn(true);
        when(userProvider.getRegisteredUserId()).thenReturn(TEST_USER_ID);
        when(tokenRepository.findAllByUser_Id(TEST_USER_ID)).thenReturn(Collections.emptyList());

        // When
        List<StreamingPlatform> result = oauthTokenService.getAuthenticatedPlatforms();

        // Then
        assertTrue(result.isEmpty());
    }
}