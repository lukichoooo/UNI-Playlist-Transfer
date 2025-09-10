package com.khundadze.PlaylistConverter.oauthTokenTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.khundadze.PlaylistConverter.dtos.OAuthTokenResponseDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.models_db.OAuthToken;
import com.khundadze.PlaylistConverter.models_db.OAuthTokenId;
import com.khundadze.PlaylistConverter.models_db.User;
import com.khundadze.PlaylistConverter.services.OAuthTokenMapper;

class OAuthTokenMapperTest {

    private OAuthTokenMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OAuthTokenMapper();
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
        User user = new User();
        user.setId(1L);

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
}