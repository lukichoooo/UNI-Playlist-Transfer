package com.khundadze.PlaylistConverter.streamingServices;

import org.springframework.stereotype.Component;

import com.khundadze.PlaylistConverter.enums.MusicService;
import com.khundadze.PlaylistConverter.services.OAuthTokenService;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class MusicServiceManager {

    private final SpotifyService spotifyService;
    private final YouTubeService youTubeService;
    private final SoundCloudService soundCloudService;
    private final OAuthTokenService tokenService;

    // Map service name to implementation
    private Map<MusicService, IMusicService> getServiceMap() {
        return Map.of(
                MusicService.SPOTIFY, spotifyService,
                MusicService.YOUTUBE, youTubeService,
                MusicService.SOUNDCLOUD, soundCloudService);
    }

    private IMusicService resolveService(MusicService service) {
        IMusicService svc = getServiceMap().get(service);
        if (svc == null)
            throw new IllegalArgumentException("Unknown service: " + service);
        return svc;
    }

    private String getTokenOrThrow(Long userId, MusicService service) {
        String token = tokenService.getValidAccessToken(userId, service);
        if (token == null)
            throw new IllegalStateException("No valid token for " + service + " for user " + userId);
        return token;
    }

    public String getUsersPlaylists(MusicService service, Long userId) {
        IMusicService svc = resolveService(service);
        String token = getTokenOrThrow(userId, service);
        return svc.getUsersPlaylists(userId, token);
    }

    public String getPlaylistTracks(MusicService service, Long playlistId, Long userId) {
        IMusicService svc = resolveService(service);
        String token = getTokenOrThrow(userId, service);
        return svc.getPlaylistsTracks(playlistId, token);
    }

    public String createPlaylist(MusicService service, Long userId) {
        IMusicService svc = resolveService(service);
        String token = getTokenOrThrow(userId, service);
        return svc.createPlaylist(userId, token);
    }
}
