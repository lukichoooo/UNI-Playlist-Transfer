package com.khundadze.PlaylistConverter.controllers;

import com.khundadze.PlaylistConverter.dtos.ConvertPlaylistRequest;
import com.khundadze.PlaylistConverter.dtos.PlaylistSearchDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.exceptions.PlaylistConversionException;
import com.khundadze.PlaylistConverter.securityConfig.oauth2ForPlatformAuth.StateManager;
import com.khundadze.PlaylistConverter.services.CurrentUserProvider;
import com.khundadze.PlaylistConverter.services.OAuthTokenService;
import com.khundadze.PlaylistConverter.streamingServices.MusicServiceManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final CurrentUserProvider userProvider;
    private static final Logger log = LoggerFactory.getLogger(ConverterController.class);

    @GetMapping("/transferState")
    public ResponseEntity<String> getTransferState() {
        String transferState = stateManager.generateState();
        return ResponseEntity.ok(transferState);
    }

    @PostMapping("/convert")
    public ResponseEntity<Void> convertPlaylist(@RequestBody ConvertPlaylistRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principalId = userProvider.getCurrentPrincipalId();

        CompletableFuture.runAsync(() -> {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            try {
                musicServiceManager.transferPlaylist(
                        request.transferState(),
                        request.fromPlatform(),
                        request.toPlatform(),
                        request.fromPlaylistId(),
                        request.newPlaylistName()
                );
            } catch (Exception e) {
                throw new PlaylistConversionException("Conversion failed for playlist " + request.fromPlaylistId(), e);
            }
        }).exceptionally(ex -> {
            log.error("Asynchronous playlist conversion failed for principalId: {}", principalId, ex);
            return null;
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
