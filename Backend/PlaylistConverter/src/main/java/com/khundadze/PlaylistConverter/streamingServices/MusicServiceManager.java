package com.khundadze.PlaylistConverter.streamingServices;

import com.khundadze.PlaylistConverter.dtos.OAuthTokenResponseDto;
import com.khundadze.PlaylistConverter.dtos.PlaylistSearchDto;
import com.khundadze.PlaylistConverter.dtos.TargetMusicDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.exceptions.UserNotAuthorizedForStreamingPlatformException;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.services.OAuthTokenService;
import com.khundadze.PlaylistConverter.streamingServices.progressBar.ProgressService;
import com.khundadze.PlaylistConverter.streamingServices.progressBar.TransferProgress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class MusicServiceManager {

    private final StreamingPlatformRegistry registry;
    private final OAuthTokenService tokenService;
    private final ProgressService progressService;

    private OAuthTokenResponseDto getTokenOrThrow(StreamingPlatform service) {
        OAuthTokenResponseDto tokenDto = tokenService.getValidAccessTokenDto(service);
        if (tokenDto == null) {
            throw new UserNotAuthorizedForStreamingPlatformException("No valid token for " + service);
        }
        return tokenDto;
    }

    // ASC
    public List<PlaylistSearchDto> getUsersPlaylists(StreamingPlatform platform) {
        MusicService svc = registry.getService(platform);
        String token = getTokenOrThrow(platform).accessToken();
        return svc.getUsersPlaylists(token).stream()
                .sorted(Comparator.comparing(PlaylistSearchDto::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Playlist createPlaylist(StreamingPlatform platform, String playlistName, List<String> trackIds) {
        MusicService svc = registry.getService(platform);
        String token = getTokenOrThrow(platform).accessToken();
        return svc.createPlaylist(token, playlistName, trackIds);
    }

    public Playlist transferPlaylist(
            String transferState,
            StreamingPlatform fromPlatform,
            StreamingPlatform toPlatform,
            String fromPlaylistId,
            String newPlaylistName) {

        // Source service and token
        MusicService svcFrom = registry.getService(fromPlatform);
        String fromToken = getTokenOrThrow(fromPlatform).accessToken();

        // Target service and token
        MusicService svcTo = registry.getService(toPlatform);
        String toToken = getTokenOrThrow(toPlatform).accessToken();

        // Fetch tracks from source
        List<TargetMusicDto> fromTracks = svcFrom.getPlaylistsTracks(fromToken, fromPlaylistId);

        AtomicInteger trackCount = new AtomicInteger();
        // Find tracks in target platform and collect IDs
        List<String> toTrackIds = fromTracks.stream()
                .map(track -> {
                    String trackId = svcTo.findTrackId(toToken, track);
                    logToFrontend(transferState, track, trackId, trackCount.getAndIncrement(), fromTracks.size());
                    return trackId;
                })
                .filter(id -> id != null && !id.isEmpty())
                .toList();

        // Default playlist name if not provided
        if (newPlaylistName == null || newPlaylistName.isEmpty()) {
            newPlaylistName = "Playlist From " + fromPlatform.name();
        }

        // Create playlist in target platform
        Playlist createdPlaylist = svcTo.createPlaylist(toToken, newPlaylistName, toTrackIds);

        progressService.sendProgress(
                transferState,
                new TransferProgress(
                        "Completed: Playlist created successfully!",
                        createdPlaylist.getMusicCount(), // Current is now the same as total
                        createdPlaylist.getMusicCount()  // Total remains the same
                )
        );

        return createdPlaylist;
    }

    private void logToFrontend(String transferState, TargetMusicDto track, String trackId, int currentTrack, int totalTracks) {
        progressService.sendProgress(
                transferState,
                new TransferProgress(
                        (trackId != null && !trackId.isEmpty() ?
                                "Found:" : "Unavilable:") + " - " + track.name(),
                        currentTrack,
                        totalTracks)
        );
    }

}
