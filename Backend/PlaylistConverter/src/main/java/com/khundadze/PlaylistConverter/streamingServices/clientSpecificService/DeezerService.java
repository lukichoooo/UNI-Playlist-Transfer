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
import java.util.stream.Collectors;

@Service
public class DeezerService extends MusicService {

    public DeezerService(
            @Qualifier("deezerWebClient") WebClient webClient,
            MusicMatcher matcher,
            MusicMapper mapper,
            MusicQueryBuilder queryBuilder
    ) {
        super(matcher, mapper, webClient, queryBuilder);
    }

    private final String API_BASE = "https://api.deezer.com";

    @Override
    public List<PlaylistSearchDto> getUsersPlaylists(String accessToken) {
        String uri = UriComponentsBuilder
                .fromHttpUrl(API_BASE + "/user/me/playlists")
                .queryParam("access_token", accessToken)
                .build().toUriString();

        Map<String, Object> response = getRequest(uri, accessToken, Map.class);
        List<PlaylistSearchDto> playlists = new ArrayList<>();

        if (response != null && response.containsKey("data")) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("data");
            if (items != null) {
                for (Map<String, Object> item : items) {
                    String id = String.valueOf(item.get("id"));
                    String title = (String) item.get("title");
                    Integer trackCount = (Integer) item.get("nb_tracks");
                    playlists.add(new PlaylistSearchDto(id, title, trackCount));
                }
            }
        }
        return playlists;
    }

    @Override
    public Playlist createPlaylist(String accessToken, String playlistName, List<String> trackIds) {
        String userId = getCurrentUserId(accessToken);

        // 1. Create the new playlist
        String createPlaylistUri = UriComponentsBuilder
                .fromHttpUrl(API_BASE + "/user/" + userId + "/playlists")
                .queryParam("access_token", accessToken)
                .queryParam("title", playlistName)
                .build().toUriString();
        Map<String, Object> createResponse = postRequest(createPlaylistUri, accessToken, null, Map.class);
        if (createResponse == null || !createResponse.containsKey("id")) {
            throw new RuntimeException("Failed to create playlist on Deezer.");
        }
        String playlistId = String.valueOf(createResponse.get("id"));

        // 2. Add tracks to the newly created playlist
        if (trackIds != null && !trackIds.isEmpty()) {
            String addTracksUri = UriComponentsBuilder
                    .fromHttpUrl(API_BASE + "/playlist/" + playlistId + "/tracks")
                    .queryParam("access_token", accessToken)
                    .queryParam("songs", String.join(",", trackIds))
                    .build().toUriString();
            postRequest(addTracksUri, accessToken, null, Map.class);
        }

        // 3. Build the final response object
        List<Music> musics = trackIds.stream()
                .map(id -> Music.builder().id(id).name(id).build())
                .toList();

        return Playlist.builder()
                .id(playlistId)
                .name(playlistName)
                .streamingPlatform(StreamingPlatform.DEEZER)
                .musics(musics)
                .build();
    }

    private String getCurrentUserId(String accessToken) {
        String uri = UriComponentsBuilder
                .fromHttpUrl(API_BASE + "/user/me")
                .queryParam("access_token", accessToken)
                .build().toUriString();
        Map<String, Object> userProfile = getRequest(uri, accessToken, Map.class);
        if (userProfile == null || !userProfile.containsKey("id")) {
            throw new RuntimeException("Failed to retrieve user ID from Deezer.");
        }
        return String.valueOf(userProfile.get("id"));
    }

    @Override
    public List<TargetMusicDto> getPlaylistsTracks(String accessToken, String playlistId) {
        String uri = UriComponentsBuilder
                .fromHttpUrl(API_BASE + "/playlist/" + playlistId + "/tracks")
                .queryParam("access_token", accessToken)
                .build().toUriString();
        Map<String, Object> response = getRequest(uri, accessToken, Map.class);
        List<TargetMusicDto> musics = new ArrayList<>();

        if (response != null && response.containsKey("data")) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("data");
            if (items != null) {
                for (Map<String, Object> track : items) {
                    String id = String.valueOf(track.get("id"));
                    String name = (String) track.get("title");
                    String artist = null;
                    if (track.containsKey("artist")) {
                        Map<String, Object> artistMap = (Map<String, Object>) track.get("artist");
                        artist = (String) artistMap.get("name");
                    }
                    String album = null;
                    if (track.containsKey("album")) {
                        Map<String, Object> albumMap = (Map<String, Object>) track.get("album");
                        album = (String) albumMap.get("title");
                    }
                    String isrc = (String) track.get("isrc");
                    String duration = String.valueOf(track.get("duration"));
                    String description = (String) track.get("title_version"); // Deezer has a title_version field that can be used as a description

                    musics.add(new TargetMusicDto(id, name, artist, album, isrc, duration, List.of(description)));
                }
            }
        }
        return musics;
    }

    @Override
    public String findTrackId(String accessToken, TargetMusicDto target, StreamingPlatform fromPlatform) {
        if (target == null || target.name() == null) return null;

        String query = queryBuilder.buildQuery(target, fromPlatform, StreamingPlatform.DEEZER);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(API_BASE + "/search")
                .queryParam("q", query);

        String searchUrl = builder.build().toUriString();
        Map<String, Object> response = getRequest(searchUrl, accessToken, Map.class);
        if (response == null || !response.containsKey("data")) return null;

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("data");
        if (items == null || items.isEmpty()) return null;

        List<ResultMusicDto> results = items.stream()
                .map(track -> {
                    String id = String.valueOf(track.get("id"));
                    String name = (String) track.get("title");
                    String artist = null;
                    if (track.containsKey("artist")) {
                        Map<String, Object> artistMap = (Map<String, Object>) track.get("artist");
                        artist = (String) artistMap.get("name");
                    }
                    String album = null;
                    if (track.containsKey("album")) {
                        Map<String, Object> albumMap = (Map<String, Object>) track.get("album");
                        album = (String) albumMap.get("title");
                    }
                    String isrc = (String) track.get("isrc");
                    String duration = String.valueOf(track.get("duration"));

                    Music music = Music.builder()
                            .id(id)
                            .name(name)
                            .artist(artist)
                            .album(album)
                            .isrc(isrc)
                            .duration(duration)
                            .build();
                    return mapper.toResultMusicDto(music);
                })
                .collect(Collectors.toList());

        Music bestMatch = matcher.bestMatch(target, results);
        return (bestMatch != null) ? bestMatch.getId() : null;
    }
}