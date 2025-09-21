package com.khundadze.PlaylistConverter.streamingServices.clientSpecificService;

import com.khundadze.PlaylistConverter.dtos.PlaylistSearchDto;
import com.khundadze.PlaylistConverter.dtos.ResultMusicDto;
import com.khundadze.PlaylistConverter.dtos.TargetMusicDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.models.Music;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.services.MusicMapper;
import com.khundadze.PlaylistConverter.streamingServices.MusicService;
import com.khundadze.PlaylistConverter.streamingServices.algorithm.searchQuery.MusicQueryBuilder;
import com.khundadze.PlaylistConverter.streamingServices.algorithm.searchResultsMatching.MusicMatcher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SoundCloudService extends MusicService {

    public SoundCloudService(
            @Qualifier("soundCloudWebClient") WebClient webClient,
            MusicMatcher matcher,
            MusicMapper mapper,
            MusicQueryBuilder queryBuilder
    ) {
        super(matcher, mapper, webClient, queryBuilder);
    }


    private final String API_BASE = "https://api.soundcloud.com";

    @Override
    public List<PlaylistSearchDto> getUsersPlaylists(String accessToken) {
        String uri = API_BASE + "/me/playlists?limit=200&linked_partitioning=1";
        List<PlaylistSearchDto> playlists = new ArrayList<>();

        while (uri != null) {
            Map<String, Object> page = getRequest(uri, accessToken, Map.class);

            List<Map<String, Object>> collection = (List<Map<String, Object>>) page.get("collection");
            if (collection != null) {
                for (Map<String, Object> item : collection) {
                    String id = String.valueOf(item.get("id"));
                    String title = (String) item.get("title");

                    Integer itemCount = item.get("track_count") != null
                            ? ((Number) item.get("track_count")).intValue()
                            : 0;

                    playlists.add(new PlaylistSearchDto(id, title, itemCount));
                }
            }

            uri = (String) page.get("next_href"); // null means no more pages
        }
        return playlists;
    }


    @Override
    public Playlist createPlaylist(String accessToken, String playlistName, List<String> trackIds) {
        String url = API_BASE + "/playlists";
        Map<String, Object> body = Map.of(
                "playlist", Map.of(
                        "title", playlistName,
                        "sharing", "private",
                        "tracks", trackIds.stream().map(id -> Map.of("id", id)).toList()
                )
        );

        Map<String, Object> response = postRequest(url, accessToken, body, Map.class);
        if (response == null || !response.containsKey("id")) {
            throw new RuntimeException("Failed to create playlist on SoundCloud");
        }

        String playlistId = String.valueOf(response.get("id"));
        List<Music> musics = trackIds.stream()
                .map(id -> Music.builder().id(id).name(id).build())
                .toList();

        return Playlist.builder()
                .id(playlistId)
                .name(playlistName)
                .streamingPlatform(StreamingPlatform.SOUNDCLOUD)
                .musics(musics)
                .build();
    }

    @Override
    public List<TargetMusicDto> getPlaylistsTracks(String accessToken, String playlistId) {
        Map<String, Object> response = getRequest(API_BASE + "/playlists/" + playlistId, accessToken, Map.class);
        List<TargetMusicDto> tracks = new ArrayList<>();

        if (response != null) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("tracks");
            if (items != null) {
                for (Map<String, Object> track : items) {
                    String id = String.valueOf(track.get("id"));
                    String title = (String) track.get("title");

                    // handle nested "user" object
                    Map<String, Object> userMap = (Map<String, Object>) track.get("user");
                    String artist = userMap != null ? (String) userMap.get("username") : null;

                    // handle nested "playlist" object
                    Map<String, Object> playlistMap = (Map<String, Object>) track.get("playlist");
                    String album = playlistMap != null ? (String) playlistMap.get("title") : null;

                    String isrc = (String) track.get("track_code");
                    String duration = track.get("duration") != null ? String.valueOf(track.get("duration")) : null;
                    String description = (String) track.get("description");

                    Music music = Music.builder()
                            .id(id)
                            .name(title)
                            .artist(artist)
                            .album(album)
                            .isrc(isrc)
                            .duration(duration)
                            .description(description)
                            .build();
                    tracks.add(mapper.toTargetMusicDto(music));
                }
            }
        }
        return tracks;
    }


    @Override
    public String findTrackId(String accessToken, TargetMusicDto target, StreamingPlatform fromPlatform) {
        if (target == null || target.name() == null) return null;

        String query = queryBuilder.buildQuery(target, fromPlatform, StreamingPlatform.SOUNDCLOUD);

        String searchUrl = UriComponentsBuilder
                .fromHttpUrl(API_BASE + "/tracks")
                .queryParam("q", query)
                .queryParam("limit", 50)
                .build().toUriString();

        List<Map<String, Object>> response = getRequest(searchUrl, accessToken, List.class);
        if (response == null) return null;

        List<ResultMusicDto> results = new ArrayList<>();
        for (Map<String, Object> track : response) {
            String id = String.valueOf(track.get("id"));
            String title = (String) track.get("title");

            // handle nested "user" object
            Map<String, Object> userMap = (Map<String, Object>) track.get("user");
            String artist = userMap != null ? (String) userMap.get("username") : null;

            // handle nested "playlist" object
            Map<String, Object> playlistMap = (Map<String, Object>) track.get("playlist");
            String album = playlistMap != null ? (String) playlistMap.get("title") : null;

            String isrc = (String) track.get("track_code");
            String duration = track.get("duration") != null ? String.valueOf(track.get("duration")) : null;
            String description = (String) track.get("description");

            Music music = Music.builder()
                    .id(id)
                    .name(title)
                    .artist(artist)
                    .album(album)
                    .isrc(isrc)
                    .duration(duration)
                    .description(description)
                    .build();
            results.add(mapper.toResultMusicDto(music));
        }
        Music bestMatch = matcher.bestMatch(target, results);
        return (bestMatch != null) ? bestMatch.getId() : null;
    }


}
