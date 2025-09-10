package com.khundadze.PlaylistConverter.services;

import org.springframework.stereotype.Component;

import com.khundadze.PlaylistConverter.dtos.OAuthTokenResponseDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.models_db.OAuthToken;
import com.khundadze.PlaylistConverter.models_db.OAuthTokenId;
import com.khundadze.PlaylistConverter.models_db.User;

@Component
public class OAuthTokenMapper {

    public OAuthTokenId toId(User user, StreamingPlatform service) {
        if (user == null || service == null) {
            throw new IllegalArgumentException("User and service must not be null");
        }
        return new OAuthTokenId(user.getId(), service);
    }

    public OAuthTokenResponseDto toOAuthTokenResponseDto(OAuthToken token) {
        return new OAuthTokenResponseDto(token.getAccessToken(), token.getPlatform());
    }
}
