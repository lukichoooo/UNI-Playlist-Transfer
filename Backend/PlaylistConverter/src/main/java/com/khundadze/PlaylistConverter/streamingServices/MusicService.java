package com.khundadze.PlaylistConverter.streamingServices;

import com.khundadze.PlaylistConverter.dtos.PlaylistSearchDto;
import com.khundadze.PlaylistConverter.dtos.TargetMusicDto;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.services.MusicMapper;
import lombok.AllArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@AllArgsConstructor
public abstract class MusicService {

    protected final MusicMatcher matcher;
    protected final MusicMapper mapper;
    protected final WebClient webClient;

    protected <T> T get(String uri, String accessToken, Class<T> clazz) {
        return webClient.get()
                .uri(uri)
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(clazz)
                .block(); // synchronous style
    }

    protected <T> T post(String uri, String accessToken, Object body, Class<T> clazz) {
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
    // build the music object
    // MAX_DESCRIPTION_LENGTH = starting with 300 + ending with 150
    // store description as list of words

    public abstract Playlist createPlaylist(String accessToken, String playlistName, List<String> trackIds);

    public abstract String findTrackId(String accessToken, TargetMusicDto target);
    // must search with, name + artist  OR  name + album
    // if any of musics fields are null, search for them in description
    // store every word of that description in musics hashset "descriptionKeywords" (lowercase words)
    // now compare every word in targetsDescription with every word in keywords

}
