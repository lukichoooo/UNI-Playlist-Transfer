package com.khundadze.PlaylistConverter.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.khundadze.PlaylistConverter.dtos.ConvertPlaylistRequest;
import com.khundadze.PlaylistConverter.dtos.ListPlaylistsRequest;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.services.OAuthTokenService;
import com.khundadze.PlaylistConverter.streamingServices.MusicServiceManager;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/converter")
@RequiredArgsConstructor
public class ConverterController {

    private final MusicServiceManager musicServiceManager;
    private final OAuthTokenService tokenService;

    @PostMapping("/convert")
    public ResponseEntity<Playlist> convertPlaylist(@RequestBody ConvertPlaylistRequest request) {
        Playlist playlist = musicServiceManager.transferPlaylist(
                request.fromPlatform(),
                request.toPlatform(),
                request.fromPlaylistId(),
                request.toPlaylistId());
        return ResponseEntity.ok(playlist);
    }

    @PostMapping("/list")
    public ResponseEntity<List<Playlist>> getPlaylists(@RequestBody ListPlaylistsRequest request) {
        List<Playlist> playlists = musicServiceManager.getUsersPlaylists(request.platform());
        return ResponseEntity.ok(playlists);
    }

    @GetMapping("/authenticatedPlatforms")
    public ResponseEntity<List<StreamingPlatform>> getAuthenticatedPlatforms() {
        List<StreamingPlatform> platforms = tokenService.getAuthenticatedPlatforms();
        return ResponseEntity.ok(platforms);
    }
}
