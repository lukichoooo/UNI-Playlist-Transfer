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
    public Playlist transferPlaylist(
            StreamingPlatform fromPlatform,
            StreamingPlatform toPlatform,
            String fromPlaylistId,
            String newPlaylistName) {

        // Source service and token
        IMusicService svcFrom = registry.getService(fromPlatform);
        String fromToken = getTokenOrThrow(fromPlatform).accessToken();

        // Target service and token
        IMusicService svcTo = registry.getService(toPlatform);
        String toToken = getTokenOrThrow(toPlatform).accessToken();

        // Fetch tracks from source
        List<Music> tracks = svcFrom.getPlaylistsTracks(fromToken, fromPlaylistId);
        for (Music track : tracks) {
            System.out.println(track.getName() + " - " + track.getId());
        }

        // Extract track IDs
        List<String> trackIds = tracks.stream()
                .map(Music::getId)
                .toList();

        // Default playlist name if not provided
        if (newPlaylistName == null || newPlaylistName.isEmpty()) {
            newPlaylistName = "Playlist From " + fromPlatform.name();
        }

        // Create playlist on target platform
        Playlist playlist = svcTo.createPlaylist(toToken, newPlaylistName, trackIds);

        return playlist;
    }

}
