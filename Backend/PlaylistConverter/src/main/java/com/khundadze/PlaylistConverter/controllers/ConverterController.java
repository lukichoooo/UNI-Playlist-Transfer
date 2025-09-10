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
    @PostMapping("/convert")
    public ResponseEntity<Playlist> convertPlaylist(@RequestBody StreamingPlatform fromPlatform,
            StreamingPlatform toPlatform, Long fromId,
            Long toId) {
        Playlist playlist = musicServiceManager.transferPlaylist(fromPlatform, toPlatform, fromId, toId);
        return ResponseEntity.ok(playlist);
    }

    /**
     * TODO: STORE PLAYLISTS IN CACHE
     */
    @GetMapping("/list")
    public ResponseEntity<List<Playlist>> getPlaylists(@RequestBody StreamingPlatform service,
            String accessToken) {
        List<Playlist> playlists = musicServiceManager.getUsersPlaylists(service);
        return ResponseEntity.ok(playlists);
    }

    @GetMapping("/authenticatedPlatforms")
    public ResponseEntity<List<StreamingPlatform>> getAuthenticatedPlatforms() {
        List<StreamingPlatform> platforms = tokenService.getAuthenticatedPlatforms();
        return ResponseEntity.ok(platforms);
    }

}
