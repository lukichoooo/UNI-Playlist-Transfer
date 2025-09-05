package com.khundadze.PlaylistConverter.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.khundadze.PlaylistConverter.enums.MusicService;
import com.khundadze.PlaylistConverter.models_db.OAuthToken;
import com.khundadze.PlaylistConverter.repo.OAuthTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthTokenService { // more descriptive name

    private final OAuthTokenRepository tokenRepository;
    private final OAuthTokenMapper oauthTokenIdMapper;

    // public OAuthToken save(Long userId, MusicService service, OAuthToken token) {

    // }

    // public String getValidAccessToken(Long userId, MusicService service) {

    // }

    // public void deleteOAuthTokenForUser(Long userId, MusicService service) {

    // }

    public List<OAuthToken> getAllOAuthTokensForUser(Long userId) {
        return tokenRepository.findAllByUser_Id(userId);
    }
}
