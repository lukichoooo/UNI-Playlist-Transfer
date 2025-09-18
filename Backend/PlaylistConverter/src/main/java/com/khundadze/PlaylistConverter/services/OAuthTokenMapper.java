package com.khundadze.PlaylistConverter.services;

import com.khundadze.PlaylistConverter.dtos.OAuthTokenResponseDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.models_db.OAuthToken;
import com.khundadze.PlaylistConverter.models_db.OAuthTokenId;
import com.khundadze.PlaylistConverter.models_db.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuthTokenMapper {

    private final OAuthTokenEncryptor encryptor;

    public OAuthTokenId toId(User user, StreamingPlatform service) {
        if (user == null || service == null) {
            throw new IllegalArgumentException("User and service must not be null");
        }
        return new OAuthTokenId(user.getId(), service);
    }

    public OAuthTokenResponseDto toOAuthTokenResponseDto(OAuthToken token) {
        return new OAuthTokenResponseDto(token.getAccessToken(), token.getPlatform());
    }

    public OAuthToken encryptToken(OAuthToken token) {
        if (token.getAccessToken() != null) {
            token.setAccessToken(encryptor.encrypt(token.getAccessToken()));
        }
        if (token.getRefreshToken() != null) {
            token.setRefreshToken(encryptor.encrypt(token.getRefreshToken()));
        }
        return token;
    }

    public OAuthTokenResponseDto decryptTokenDto(OAuthTokenResponseDto tokenDto) {
        String decryptedAccess = tokenDto.accessToken() != null
                ? encryptor.decrypt(tokenDto.accessToken())
                : null;

        return new OAuthTokenResponseDto(decryptedAccess, tokenDto.service());
    }

}
