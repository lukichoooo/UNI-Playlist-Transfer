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

@Service
public class SpotifyService extends MusicService {

    public SpotifyService(
            @Qualifier("spotifyWebClient") WebClient webClient,
            MusicMatcher matcher,
            MusicMapper mapper,
            MusicQueryBuilder queryBuilder

    ) {
        super(matcher, mapper, webClient, queryBuilder);
    }

    @Override
    public List<PlaylistSearchDto> getUsersPlaylists(String accessToken) {
        return null;
    }

    @Override
    public List<TargetMusicDto> getPlaylistsTracks(String accessToken, String playlistId) {
        return null;
    }

    @Override
    public Playlist createPlaylist(String accessToken, String playlistName, List<String> trackIds) {
        return null;
    }


    @Override
    public String findTrackId(String accesToken, TargetMusicDto target, StreamingPlatform fromPlatform) {
        return null;
    }
}
