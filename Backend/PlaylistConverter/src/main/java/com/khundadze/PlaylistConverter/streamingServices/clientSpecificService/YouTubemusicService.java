package com.khundadze.PlaylistConverter.streamingServices.clientSpecificService;

import com.khundadze.PlaylistConverter.dtos.PlaylistSearchDto;
import com.khundadze.PlaylistConverter.dtos.TargetMusicDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.services.MusicMapper;
import com.khundadze.PlaylistConverter.streamingServices.MusicService;
import com.khundadze.PlaylistConverter.streamingServices.algorithm.searchQuery.MusicQueryBuilder;
import com.khundadze.PlaylistConverter.streamingServices.algorithm.searchResultsMatching.MusicMatcher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * A service class that acts as a delegate for the YouTubeService.
 * This class forwards all method calls to an instance of YouTubeService,
 * effectively reusing its functionality.
 */
@Service
public class YouTubemusicService extends MusicService {

    private final YouTubeService youtubeService;

    public YouTubemusicService(
            MusicMatcher matcher,
            MusicMapper mapper,
            @Qualifier("youTubeWebClient") WebClient webClient,
            MusicQueryBuilder queryBuilder,
            YouTubeService youtubeService
    ) {
        super(matcher, mapper, webClient, queryBuilder);
        this.youtubeService = youtubeService;
    }

    @Override
    public List<PlaylistSearchDto> getUsersPlaylists(String accessToken) {
        return youtubeService.getUsersPlaylists(accessToken);
    }

    @Override
    public Playlist createPlaylist(String accessToken, String playlistName, List<String> trackIds) {
        return youtubeService.createPlaylist(accessToken, playlistName, trackIds);
    }

    @Override
    public List<TargetMusicDto> getPlaylistsTracks(String accessToken, String playlistId) {
        return youtubeService.getPlaylistsTracks(accessToken, playlistId);
    }

    @Override
    public String findTrackId(String accessToken, TargetMusicDto target, StreamingPlatform fromPlatform) {
        return youtubeService.findTrackId(accessToken, target, fromPlatform);
    }
}
