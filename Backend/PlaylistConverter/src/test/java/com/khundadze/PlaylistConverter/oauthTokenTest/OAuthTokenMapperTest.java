package com.khundadze.PlaylistConverter.oauthTokenTest;

import com.khundadze.PlaylistConverter.dtos.OAuthTokenResponseDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.models_db.OAuthToken;
import com.khundadze.PlaylistConverter.models_db.OAuthTokenId;
import com.khundadze.PlaylistConverter.models_db.User;
import com.khundadze.PlaylistConverter.services.OAuthTokenEncryptor;
import com.khundadze.PlaylistConverter.services.OAuthTokenMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OAuthTokenMapperTest {

    private OAuthTokenMapper mapper;

    @BeforeEach
    void setUp() {
        // Provide a real encryptor
        OAuthTokenEncryptor encryptor = new OAuthTokenEncryptor("test-password", "12345678");
        mapper = new OAuthTokenMapper(encryptor);
    }

    @Test
    void testToId_validUserAndService_returnsId() {
        User user = new User();
        user.setId(42L);

        OAuthTokenId id = mapper.toId(user, StreamingPlatform.SPOTIFY);

        assertNotNull(id);
        assertEquals(42L, id.getUserId());
        assertEquals(StreamingPlatform.SPOTIFY, id.getPlatform());
    }

    @Test
    void testToId_nullUser_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> mapper.toId(null, StreamingPlatform.SPOTIFY));
        assertEquals("User and service must not be null", ex.getMessage());
    }

    @Test
    void testToId_nullService_throwsException() {
        User user = new User();
        user.setId(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> mapper.toId(user, null));
        assertEquals("User and service must not be null", ex.getMessage());
    }

    @Test
    void testToOAuthTokenResponseDto_mapsCorrectly() {
        OAuthToken token = OAuthToken.builder()
                .id(new OAuthTokenId(1L, StreamingPlatform.YOUTUBE))
                .accessToken("abc123")
                .refreshToken("refreshMe")
                .build();

        OAuthTokenResponseDto dto = mapper.toOAuthTokenResponseDto(token);

        assertNotNull(dto);
        assertEquals("abc123", dto.accessToken());
        assertEquals(StreamingPlatform.YOUTUBE, dto.service());
    }

    // ===================== NEW TESTS =====================

    @Test
    void testEncryptToken_encryptsAccessAndRefreshTokens() {
        OAuthToken token = OAuthToken.builder()
                .id(new OAuthTokenId(1L, StreamingPlatform.SPOTIFY))
                .accessToken("my-access")
                .refreshToken("my-refresh")
                .build();

        OAuthToken encrypted = mapper.encryptToken(token);

        assertNotNull(encrypted.getAccessToken());
        assertNotNull(encrypted.getRefreshToken());
        assertNotEquals("my-access", encrypted.getAccessToken());
        assertNotEquals("my-refresh", encrypted.getRefreshToken());
    }

    @Test
    void testDecryptTokenDto_decryptsAccessTokenCorrectly() {
        OAuthToken token = OAuthToken.builder()
                .id(new OAuthTokenId(1L, StreamingPlatform.SPOTIFY))
                .accessToken("my-access")
                .build();

        OAuthToken encrypted = mapper.encryptToken(token);
        OAuthTokenResponseDto dto = mapper.toOAuthTokenResponseDto(encrypted);

        OAuthTokenResponseDto decrypted = mapper.decryptTokenDto(dto);

        assertEquals("my-access", decrypted.accessToken());
        assertEquals(StreamingPlatform.SPOTIFY, decrypted.service());
    }

    @Test
    void testEncryptThenDecrypt_roundTrip() {
        OAuthToken token = OAuthToken.builder()
                .id(new OAuthTokenId(1L, StreamingPlatform.SPOTIFY))
                .accessToken("roundtrip-access")
                .refreshToken("roundtrip-refresh")
                .build();

        OAuthToken encrypted = mapper.encryptToken(token);
        OAuthTokenResponseDto dto = mapper.toOAuthTokenResponseDto(encrypted);
        OAuthTokenResponseDto decrypted = mapper.decryptTokenDto(dto);

        assertEquals("roundtrip-access", decrypted.accessToken());
    }
}
