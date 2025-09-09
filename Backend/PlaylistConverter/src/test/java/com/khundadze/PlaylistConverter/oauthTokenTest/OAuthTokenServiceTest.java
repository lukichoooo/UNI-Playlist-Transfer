package com.khundadze.PlaylistConverter.oauthTokenTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.khundadze.PlaylistConverter.services.OAuthTokenMapper;
import com.khundadze.PlaylistConverter.services.OAuthTokenService;

class OAuthTokenServiceTest {

        @Mock
        private OAuthTokenRepository tokenRepository;

        @Mock
        private OAuthTokenMapper mapper;

        @InjectMocks
        private OAuthTokenService service;

        private AutoCloseable closeable;

        @BeforeEach
        void init() {
                closeable = MockitoAnnotations.openMocks(this);
        }

        @Test
        void save_shouldUpdateExistingToken() {
                OAuthTokenId id = new OAuthTokenId(1L, StreamingPlatform.SPOTIFY);
                OAuthToken existing = OAuthToken.builder()
                                .id(id)
                                .accessToken("old")
                                .refreshToken("oldRef")
                                .expiresAt(Instant.now())
                                .build();

                when(tokenRepository.findByIdUserIdAndIdService(1L, StreamingPlatform.SPOTIFY))
                                .thenReturn(Optional.of(existing));
                when(tokenRepository.save(existing)).thenReturn(existing);
                when(mapper.toOAuthTokenResponseDto(existing))
                                .thenReturn(new OAuthTokenResponseDto("newAccess", StreamingPlatform.SPOTIFY));

                OAuthTokenResponseDto result = service.save(1L, StreamingPlatform.SPOTIFY, "newAccess", "newRef",
                                Instant.now().plusSeconds(100));

                assertEquals("newAccess", result.accessToken());
                verify(tokenRepository).save(existing);
        }

        @Test
        void save_shouldCreateNewToken() {
                when(tokenRepository.findByIdUserIdAndIdService(2L, StreamingPlatform.YOUTUBE))
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

                OAuthTokenResponseDto result = service.save(2L, StreamingPlatform.YOUTUBE, "acc", "ref", Instant.now());

                assertEquals("acc", result.accessToken());
                assertEquals(StreamingPlatform.YOUTUBE, result.service());
                verify(tokenRepository).save(any(OAuthToken.class));
        }

        @Test
        void getValidAccessTokenDto_shouldReturnDtoIfValid() {
                OAuthTokenId id = new OAuthTokenId(1L, StreamingPlatform.SOUNDCLOUD);
                OAuthToken token = OAuthToken.builder()
                                .id(id)
                                .accessToken("valid")
                                .expiresAt(Instant.now().plusSeconds(60))
                                .build();

                when(tokenRepository.findByIdUserIdAndIdService(1L, StreamingPlatform.SOUNDCLOUD))
                                .thenReturn(Optional.of(token));
                when(mapper.toOAuthTokenResponseDto(token))
                                .thenReturn(new OAuthTokenResponseDto("valid", StreamingPlatform.SOUNDCLOUD));

                OAuthTokenResponseDto result = service.getValidAccessTokenDto(1L, StreamingPlatform.SOUNDCLOUD);

                assertNotNull(result);
                assertEquals("valid", result.accessToken());
                assertEquals(StreamingPlatform.SOUNDCLOUD, result.service());
        }

        @Test
        void getValidAccessTokenDto_shouldReturnNullIfExpired() {
                OAuthTokenId id = new OAuthTokenId(1L, StreamingPlatform.SOUNDCLOUD);
                OAuthToken token = OAuthToken.builder()
                                .id(id)
                                .accessToken("expired")
                                .expiresAt(Instant.now().minusSeconds(10))
                                .build();

                when(tokenRepository.findByIdUserIdAndIdService(1L, StreamingPlatform.SOUNDCLOUD))
                                .thenReturn(Optional.of(token));

                assertNull(service.getValidAccessTokenDto(1L, StreamingPlatform.SOUNDCLOUD));
        }

        @Test
        void deleteOAuthTokenForUser_shouldDeleteIfExists() {
                OAuthTokenId id = new OAuthTokenId(1L, StreamingPlatform.SPOTIFY);
                OAuthToken token = OAuthToken.builder().id(id).build();

                when(tokenRepository.findByIdUserIdAndIdService(1L, StreamingPlatform.SPOTIFY))
                                .thenReturn(Optional.of(token));

                service.deleteOAuthTokenForUser(1L, StreamingPlatform.SPOTIFY);

                verify(tokenRepository).delete(token);
        }

        @Test
        void getAllOAuthTokensForUser_shouldReturnDtos() {
                OAuthTokenId id1 = new OAuthTokenId(5L, StreamingPlatform.SPOTIFY);
                OAuthToken token1 = OAuthToken.builder().id(id1).accessToken("a1").build();

                OAuthTokenId id2 = new OAuthTokenId(5L, StreamingPlatform.YOUTUBE);
                OAuthToken token2 = OAuthToken.builder().id(id2).accessToken("a2").build();

                when(tokenRepository.findAllByUser_Id(5L)).thenReturn(List.of(token1, token2));

                when(mapper.toOAuthTokenResponseDto(token1))
                                .thenReturn(new OAuthTokenResponseDto("a1", StreamingPlatform.SPOTIFY));
                when(mapper.toOAuthTokenResponseDto(token2))
                                .thenReturn(new OAuthTokenResponseDto("a2", StreamingPlatform.YOUTUBE));

                List<OAuthTokenResponseDto> result = service.getAllOAuthTokensForUser(5L);

                assertEquals(2, result.size());
                assertEquals("a1", result.get(0).accessToken());
                assertEquals(StreamingPlatform.SPOTIFY, result.get(0).service());
                assertEquals("a2", result.get(1).accessToken());
                assertEquals(StreamingPlatform.YOUTUBE, result.get(1).service());
        }

}
