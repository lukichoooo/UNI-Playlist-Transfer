package com.khundadze.PlaylistConverter.oauthTokenTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.khundadze.PlaylistConverter.dtos.OAuthTokenResponseDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.models_db.OAuthToken;
import com.khundadze.PlaylistConverter.models_db.OAuthTokenId;
import com.khundadze.PlaylistConverter.repo.OAuthTokenRepository;
import com.khundadze.PlaylistConverter.services.CurrentUserProvider;
import com.khundadze.PlaylistConverter.services.OAuthTokenMapper;
import com.khundadze.PlaylistConverter.services.OAuthTokenService;

class OAuthTokenServiceTest {

        @Mock
        private OAuthTokenRepository tokenRepository;

        @Mock
        private OAuthTokenMapper mapper;

        @Mock
        private CurrentUserProvider userProvider;

        @InjectMocks
        private OAuthTokenService service;

        @BeforeEach
        void init() {
                MockitoAnnotations.openMocks(this);
        }

        @Test
        void save_shouldUpdateExistingToken() {
                when(userProvider.getId()).thenReturn(1L);

                OAuthTokenId id = new OAuthTokenId(1L, StreamingPlatform.SPOTIFY);
                OAuthToken existing = OAuthToken.builder()
                                .id(id)
                                .accessToken("old")
                                .refreshToken("oldRef")
                                .expiresAt(Instant.now())
                                .build();

                when(tokenRepository.findByIdUserIdAndIdPlatform(1L, StreamingPlatform.SPOTIFY))
                                .thenReturn(Optional.of(existing));
                when(tokenRepository.save(existing)).thenReturn(existing);
                when(mapper.toOAuthTokenResponseDto(existing))
                                .thenReturn(new OAuthTokenResponseDto("newAccess", StreamingPlatform.SPOTIFY));

                OAuthTokenResponseDto result = service.save(StreamingPlatform.SPOTIFY, "newAccess", "newRef",
                                Instant.now().plusSeconds(100));

                assertEquals("newAccess", result.accessToken());
                verify(tokenRepository).save(existing);
        }

        @Test
        void save_shouldCreateNewToken() {
                when(userProvider.getId()).thenReturn(2L);
                when(tokenRepository.findByIdUserIdAndIdPlatform(2L, StreamingPlatform.YOUTUBE))
                                .thenReturn(Optional.empty());

                OAuthTokenId id = new OAuthTokenId(2L, StreamingPlatform.YOUTUBE);
                OAuthToken newToken = OAuthToken.builder()
                                .id(id)
                                .accessToken("acc")
                                .refreshToken("ref")
                                .expiresAt(Instant.now())
                                .build();

                when(tokenRepository.save(any(OAuthToken.class))).thenReturn(newToken);
                when(mapper.toOAuthTokenResponseDto(newToken))
                                .thenReturn(new OAuthTokenResponseDto("acc", StreamingPlatform.YOUTUBE));

                OAuthTokenResponseDto result = service.save(StreamingPlatform.YOUTUBE, "acc", "ref", Instant.now());

                assertEquals("acc", result.accessToken());
                assertEquals(StreamingPlatform.YOUTUBE, result.service());
                verify(tokenRepository).save(any(OAuthToken.class));
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

        @Test
        void getAuthenticatedPlatforms_shouldReturnDistinctPlatforms() {
                when(userProvider.getId()).thenReturn(5L);

                OAuthTokenId id1 = new OAuthTokenId(5L, StreamingPlatform.SPOTIFY);
                OAuthToken token1 = OAuthToken.builder().id(id1).accessToken("a1").build();

                OAuthTokenId id2 = new OAuthTokenId(5L, StreamingPlatform.YOUTUBE);
                OAuthToken token2 = OAuthToken.builder().id(id2).accessToken("a2").build();

                OAuthTokenId id3 = new OAuthTokenId(5L, StreamingPlatform.SPOTIFY);
                OAuthToken token3 = OAuthToken.builder().id(id3).accessToken("a3").build();

                when(tokenRepository.findAllByUser_Id(5L)).thenReturn(List.of(token1, token2, token3));

                List<StreamingPlatform> result = service.getAuthenticatedPlatforms();

                assertEquals(2, result.size());
                assertTrue(result.contains(StreamingPlatform.SPOTIFY));
                assertTrue(result.contains(StreamingPlatform.YOUTUBE));
        }
}
