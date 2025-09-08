package com.khundadze.PlaylistConverter.streamingServices;

import org.springframework.stereotype.Component;

import com.khundadze.PlaylistConverter.dtos.OAuthTokenResponseDto;
import com.khundadze.PlaylistConverter.enums.MusicService;
import com.khundadze.PlaylistConverter.services.OAuthTokenService;
import com.khundadze.PlaylistConverter.streamingServices.clientSpecificService.SoundCloudService;
import com.khundadze.PlaylistConverter.streamingServices.clientSpecificService.SpotifyService;
import com.khundadze.PlaylistConverter.streamingServices.clientSpecificService.YouTubeService;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class MusicServiceManager {

    private final SpotifyService spotifyService;
    private final YouTubeService youTubeService;
    private final SoundCloudService soundCloudService;
    private final OAuthTokenService tokenService;

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

    private OAuthTokenResponseDto getTokenDtoOrThrow(Long userId, MusicService service) {
        OAuthTokenResponseDto tokenDto = tokenService.getValidAccessTokenDto(userId, service);
        if (tokenDto == null)
            throw new IllegalStateException("No valid token for " + service + " for user " + userId);
        return tokenDto;
    }

    public String getUsersPlaylists(MusicService service, Long userId) {
        IMusicService svc = resolveService(service);
        String token = getTokenDtoOrThrow(userId, service).accessToken();
        return svc.getUsersPlaylists(userId, token);
    }

    public String getPlaylistTracks(MusicService service, Long playlistId, Long userId) {
        IMusicService svc = resolveService(service);
        String token = getTokenDtoOrThrow(userId, service).accessToken();
        return svc.getPlaylistsTracks(playlistId, token);
    }

    public String createPlaylist(MusicService service, Long userId, String playlistName,
            java.util.List<String> trackIds) {
        IMusicService svc = resolveService(service);
        String token = getTokenDtoOrThrow(userId, service).accessToken();
        return svc.createPlaylist(userId, token, playlistName, trackIds);
    }
}
