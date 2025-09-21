package com.khundadze.PlaylistConverter.streamingServices;

import com.khundadze.PlaylistConverter.dtos.PlaylistSearchDto;
import com.khundadze.PlaylistConverter.dtos.TargetMusicDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.services.MusicMapper;
import com.khundadze.PlaylistConverter.streamingServices.algorithm.searchQuery.MusicQueryBuilder;
import com.khundadze.PlaylistConverter.streamingServices.algorithm.searchResultsMatching.MusicMatcher;
import lombok.AllArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@AllArgsConstructor
public abstract class MusicService {

    protected final MusicMatcher matcher;
    protected final MusicMapper mapper;
    protected final WebClient webClient;
    protected final MusicQueryBuilder queryBuilder;

    protected <T> T getRequest(String uri, String accessToken, Class<T> clazz) {
        return webClient.get()
                .uri(uri)
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(clazz)
                .block(); // synchronous style
    }

    protected <T> T postRequest(String uri, String accessToken, Object body, Class<T> clazz) {
        return webClient.post()
                .uri(uri)
                .headers(h -> h.setBearerAuth(accessToken))
                .bodyValue(body)
                .retrieve()
                .bodyToMono(clazz)
                .block();
    }


    public abstract List<PlaylistSearchDto> getUsersPlaylists(String accessToken);

    public abstract List<TargetMusicDto> getPlaylistsTracks(String accessToken, String playlistId);

    public abstract Playlist createPlaylist(String accessToken, String playlistName, List<String> trackIds);

    public abstract String findTrackId(String accessToken, TargetMusicDto target, StreamingPlatform fromPlatform);

}
