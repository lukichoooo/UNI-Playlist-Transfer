package com.khundadze.PlaylistConverter.streamingServices;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.khundadze.PlaylistConverter.dtos.OAuthTokenResponseDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.exceptions.UnknownStreamingPlatformException;
import com.khundadze.PlaylistConverter.models.Music;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.services.OAuthTokenService;
import com.khundadze.PlaylistConverter.streamingServices.clientSpecificService.SoundCloudService;
import com.khundadze.PlaylistConverter.streamingServices.clientSpecificService.SpotifyService;
import com.khundadze.PlaylistConverter.streamingServices.clientSpecificService.YouTubeService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MusicServiceManager {

    private final SpotifyService spotifyService;
    private final YouTubeService youTubeService;
    private final SoundCloudService soundCloudService;
    private final OAuthTokenService tokenService;

    private Map<StreamingPlatform, IMusicService> getServiceMap() {
        return Map.of(
                StreamingPlatform.SPOTIFY, spotifyService,
                StreamingPlatform.YOUTUBE, youTubeService,
                StreamingPlatform.SOUNDCLOUD, soundCloudService);
    }

    private IMusicService resolveService(StreamingPlatform service) {
        IMusicService svc = getServiceMap().get(service);
        if (svc == null)
            throw new UnknownStreamingPlatformException("Unknown service: " + service);
        return svc;
    }

    private OAuthTokenResponseDto getTokenDtoOrThrow(Long userId, StreamingPlatform service) {
        OAuthTokenResponseDto tokenDto = tokenService.getValidAccessTokenDto(userId, service);
        if (tokenDto == null)
            throw new IllegalStateException("No valid token for " + service + " for user " + userId);
        return tokenDto;
    }

    public List<Playlist> getUsersPlaylists(StreamingPlatform service, Long userId) {
        IMusicService svc = resolveService(service);
        String token = getTokenDtoOrThrow(userId, service).accessToken();
        return svc.getUsersPlaylists(userId, token);
    }

    public List<Music> getPlaylistTracks(StreamingPlatform service, Long playlistId, Long userId) {
        IMusicService svc = resolveService(service);
        String token = getTokenDtoOrThrow(userId, service).accessToken();
        return svc.getPlaylistsTracks(playlistId, token);
    }

    public Playlist createPlaylist(StreamingPlatform service, Long userId, String playlistName,
            java.util.List<String> trackIds) {
        IMusicService svc = resolveService(service);
        String token = getTokenDtoOrThrow(userId, service).accessToken();
        return svc.createPlaylist(userId, token, playlistName, trackIds);
    }
}
