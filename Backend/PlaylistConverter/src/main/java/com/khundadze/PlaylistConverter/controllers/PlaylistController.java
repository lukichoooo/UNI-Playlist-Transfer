package com.khundadze.PlaylistConverter.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.khundadze.PlaylistConverter.dtos.CreatePlaylistDto;
import com.khundadze.PlaylistConverter.dtos.GetPlaylistsDto;
import com.khundadze.PlaylistConverter.dtos.OAuthTokenResponseDto;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.services.OAuthTokenService;
import com.khundadze.PlaylistConverter.streamingServices.MusicServiceManager;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/playlist")
@RequiredArgsConstructor
public class PlaylistController { // TODO: implement

    private final MusicServiceManager musicServiceManager;
    private final OAuthTokenService tokenService;

    /**
     * Trigger OAuth2 login or return stored token for a service.
     */
    @PostMapping("/auth")
    public ResponseEntity<OAuthTokenResponseDto> authenticate(@RequestBody OAuthTokenResponseDto request) {
        return null;
    }

    /**
     * Create a playlist on the specified service.
     */
    @PostMapping("/create")
    public ResponseEntity<Playlist> createPlaylist(@RequestBody CreatePlaylistDto request) {
        return null;
    }

    /**
     * Get all playlists for a user on a specific service.
     */
    @PostMapping("/list")
    public ResponseEntity<List<String>> getPlaylists(@RequestBody GetPlaylistsDto request) {
        return null;
    }
}
