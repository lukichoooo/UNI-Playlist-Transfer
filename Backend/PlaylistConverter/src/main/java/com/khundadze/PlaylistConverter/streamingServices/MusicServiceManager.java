package com.khundadze.PlaylistConverter.streamingServices;

import java.util.List;

import org.springframework.stereotype.Component;

import com.khundadze.PlaylistConverter.dtos.OAuthTokenResponseDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.exceptions.UserNotAuthorizedForStreamingPlatformException;
import com.khundadze.PlaylistConverter.models.Music;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.services.OAuthTokenService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MusicServiceManager {

    private final StreamingServiceRegistry registry;
    private final OAuthTokenService tokenService;

    private OAuthTokenResponseDto getTokenOrThrow(StreamingPlatform service) {
        OAuthTokenResponseDto tokenDto = tokenService.getValidAccessTokenDto(service);
        if (tokenDto == null) {
            throw new UserNotAuthorizedForStreamingPlatformException("No valid token for " + service);
        }
        return tokenDto;
    }

    public List<Playlist> getUsersPlaylists(StreamingPlatform platform) {
        IMusicService svc = registry.getService(platform);
        String token = getTokenOrThrow(platform).accessToken();
        return svc.getUsersPlaylists(token);
    }

    public List<Music> getPlaylistTracks(StreamingPlatform platform, Long playlistId) {
        IMusicService svc = registry.getService(platform);
        String token = getTokenOrThrow(platform).accessToken();
        return svc.getPlaylistsTracks(playlistId, token);
    }

    public Playlist createPlaylist(StreamingPlatform platform, String playlistName, List<String> trackIds) {
        IMusicService svc = registry.getService(platform);
        String token = getTokenOrThrow(platform).accessToken();
        return svc.createPlaylist(token, playlistName, trackIds);
    }

    public Playlist transferPlaylist(StreamingPlatform fromPlatform, StreamingPlatform toPlatform, Long fromId,
            Long toId) {

        IMusicService svcFrom = registry.getService(fromPlatform);
        String fromToken = getTokenOrThrow(fromPlatform).accessToken();

        IMusicService svcTo = registry.getService(toPlatform);
        String toToken = getTokenOrThrow(toPlatform).accessToken();

        List<Music> tracks = svcFrom.getPlaylistsTracks(fromId, fromToken);

        // TODO: transfer playlist

        Playlist playlist = svcTo.createPlaylist(toToken, toToken, null);

        return null;// TODO:
    }
}
