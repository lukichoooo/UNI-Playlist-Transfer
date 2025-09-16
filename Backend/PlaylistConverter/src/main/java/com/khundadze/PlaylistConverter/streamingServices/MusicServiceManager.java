package com.khundadze.PlaylistConverter.streamingServices;

import com.khundadze.PlaylistConverter.dtos.OAuthTokenResponseDto;
import com.khundadze.PlaylistConverter.dtos.PlaylistSearchDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.exceptions.UserNotAuthorizedForStreamingPlatformException;
import com.khundadze.PlaylistConverter.models.Music;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.services.OAuthTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MusicServiceManager {

    private final StreamingPlatformRegistry registry;
    private final OAuthTokenService tokenService;

    private OAuthTokenResponseDto getTokenOrThrow(StreamingPlatform service) {
        OAuthTokenResponseDto tokenDto = tokenService.getValidAccessTokenDto(service);
        if (tokenDto == null) {
            throw new UserNotAuthorizedForStreamingPlatformException("No valid token for " + service);
        }
        return tokenDto;
    }

    // ASC
    public List<PlaylistSearchDto> getUsersPlaylists(StreamingPlatform platform) {
        IMusicService svc = registry.getService(platform);
        String token = getTokenOrThrow(platform).accessToken();
        return svc.getUsersPlaylists(token).stream()
                .sorted(Comparator.comparing(PlaylistSearchDto::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }


    public List<Music> getPlaylistTracks(StreamingPlatform platform, String playlistId) {
        IMusicService svc = registry.getService(platform);
        String token = getTokenOrThrow(platform).accessToken();
        return svc.getPlaylistsTracks(token, playlistId);
    }

    public Playlist createPlaylist(StreamingPlatform platform, String playlistName, List<String> trackIds) {
        IMusicService svc = registry.getService(platform);
        String token = getTokenOrThrow(platform).accessToken();
        return svc.createPlaylist(token, playlistName, trackIds);
    }

    // TODO: main method of the whole app
    public Playlist transferPlaylist(StreamingPlatform fromPlatform, StreamingPlatform toPlatform, String fromPlaylistId, String newPlaylistName) {

        IMusicService svcFrom = registry.getService(fromPlatform);
        String fromToken = getTokenOrThrow(fromPlatform).accessToken();

        IMusicService svcTo = registry.getService(toPlatform);
        String toToken = getTokenOrThrow(toPlatform).accessToken();

        List<Music> tracks = svcFrom.getPlaylistsTracks(fromToken, fromPlaylistId);

        // TODO: transfer playlist

        Playlist playlist = svcTo.createPlaylist(toToken, toToken, null);
        playlist.setName(newPlaylistName);

        return null;
    }
}
