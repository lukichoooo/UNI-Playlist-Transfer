package com.khundadze.PlaylistConverter.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.services.OAuthTokenService;
import com.khundadze.PlaylistConverter.streamingServices.MusicServiceManager;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/converter")
@RequiredArgsConstructor
public class ConverterController { // TODO: implement

    private final MusicServiceManager musicServiceManager;
    private final OAuthTokenService tokenService;

    /**
     * Create a playlist on the specified service.
     */
    @PostMapping("/create")
    public ResponseEntity<Playlist> createPlaylist(@RequestBody StreamingPlatform platform,
            String playlistName,
            List<String> tracks,
            String accessToken) {
        return null;
    }

    /**
     * Get all playlists for a user on a specific service.
     */
    @GetMapping("/list")
    public ResponseEntity<List<String>> getPlaylists(@RequestBody StreamingPlatform service,
            String accessToken) {
        return null;
    }

    @GetMapping("/authenticatedPlatforms")
    public ResponseEntity<List<StreamingPlatform>> getAuthenticatedPlatforms() {
        List<StreamingPlatform> platforms = tokenService.getAuthenticatedPlatforms();
        return ResponseEntity.ok(platforms);
    }

}
