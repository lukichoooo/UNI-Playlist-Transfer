package com.khundadze.PlaylistConverter.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.khundadze.PlaylistConverter.models_db.OAuthToken;
import com.khundadze.PlaylistConverter.repo.OAuthTokenRepository;
import com.khundadze.enums.MusicService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthTokenManagementService { // more descriptive name

    private final OAuthTokenRepository tokenRepository;
    private final OAuthTokenMapper oauthTokenIdMapper;

    // public OAuthToken save(Long userId, MusicService service, OAuthToken token) {

    // }

    // public String getValidAccessToken(Long userId, MusicService service) {

    // }

    // public void deleteOAuthTokenForUser(Long userId, MusicService service) {

    // }

    public List<OAuthToken> getAllOAuthTokensForUser(Long userId) {
        return tokenRepository.findAllByUserId(userId);
    }
}
