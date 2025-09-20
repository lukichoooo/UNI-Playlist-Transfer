package com.khundadze.PlaylistConverter.controllers;

import com.khundadze.PlaylistConverter.dtos.ConvertPlaylistRequest;
import com.khundadze.PlaylistConverter.dtos.PlaylistSearchDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.securityConfig.oauth2ForPlatformAuth.StateManager;
import com.khundadze.PlaylistConverter.services.OAuthTokenService;
import com.khundadze.PlaylistConverter.streamingServices.MusicServiceManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/converter")
@RequiredArgsConstructor
public class ConverterController {

    private final MusicServiceManager musicServiceManager;
    private final OAuthTokenService tokenService;
    private final StateManager stateManager;

    @GetMapping("/transferState")
    public ResponseEntity<String> getTransferState() {
        String transferState = stateManager.generateState();
        return ResponseEntity.ok(transferState);
    }

    @PostMapping("/convert")
    public ResponseEntity<Void> convertPlaylist(@RequestBody ConvertPlaylistRequest request) {
        String transferState = request.transferState();

        // ✨ 1. Get the user's login details from the web thread
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CompletableFuture.runAsync(() -> { // TODO: no need to auth
            SecurityContextHolder.getContext().setAuthentication(authentication);
            try {
                musicServiceManager.transferPlaylist(
                        transferState,
                        request.fromPlatform(),
                        request.toPlatform(),
                        request.fromPlaylistId(),
                        request.newPlaylistName()
                );
            } catch (Exception e) {
                // Log the error for debugging on the server
                // You should use a proper logger here, e.g., SLF4J
                System.err.println("Transfer failed for state " + transferState + ": " + e.getMessage());
            }
        });

        return ResponseEntity.accepted().build();
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
