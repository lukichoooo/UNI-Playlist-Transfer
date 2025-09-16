package com.khundadze.PlaylistConverter.controllers;

import com.khundadze.PlaylistConverter.dtos.ConvertPlaylistRequest;
import com.khundadze.PlaylistConverter.dtos.PlaylistSearchDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.services.OAuthTokenService;
import com.khundadze.PlaylistConverter.streamingServices.MusicServiceManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
                request.newPlaylistName());
        return ResponseEntity.ok(playlist);
    }

    @GetMapping("/playlists")
    public ResponseEntity<List<PlaylistSearchDto>> getPlaylists(@RequestParam StreamingPlatform platform) {
        List<PlaylistSearchDto> playlists = musicServiceManager.getUsersPlaylists(platform);
        return ResponseEntity.ok(playlists);
    }


    @GetMapping("/authenticatedPlatforms")
    public ResponseEntity<List<StreamingPlatform>> getAuthenticatedPlatforms() {
        List<StreamingPlatform> platforms = tokenService.getAuthenticatedPlatforms();
        return ResponseEntity.ok(platforms);
    }
}
