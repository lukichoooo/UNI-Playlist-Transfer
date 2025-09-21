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
public class SpotifyService extends MusicService {

    public SpotifyService(
            @Qualifier("spotifyWebClient") WebClient webClient,
            MusicMatcher matcher,
            MusicMapper mapper,
            MusicQueryBuilder queryBuilder

    ) {
        super(matcher, mapper, webClient, queryBuilder);
    }

    private final String API_BASE = "https://api.spotify.com/v1";

    @Override
    public List<PlaylistSearchDto> getUsersPlaylists(String accessToken) {
        String uri = API_BASE + "/me/playlists?limit=50";
        List<PlaylistSearchDto> playlists = new ArrayList<>();

        while (uri != null) {
            Map<String, Object> page = getRequest(uri, accessToken, Map.class);
            List<Map<String, Object>> items = (List<Map<String, Object>>) page.get("items");

            if (items != null) {
                for (Map<String, Object> item : items) {
                    String id = (String) item.get("id");
                    String name = (String) item.get("name");

                    Map<String, Object> tracks = (Map<String, Object>) item.get("tracks");
                    Integer trackCount = ((Number) tracks.get("total")).intValue();

                    playlists.add(new PlaylistSearchDto(id, name, trackCount));
                }
            }

            uri = (String) page.get("next");
        }

        return playlists;
    }


    @Override
    public Playlist createPlaylist(String accessToken, String playlistName, List<String> trackIds) {
        String userId = getCurrentUserId(accessToken);

        // 1. Create the new playlist
        String createPlaylistUri = API_BASE + "/users/" + userId + "/playlists";

        Map<String, Object> createBody = Map.of(
                "name", playlistName,
                "public", false
        );

        Map<String, Object> createResponse = postRequest(createPlaylistUri, accessToken, createBody, Map.class);
        if (createResponse == null || !createResponse.containsKey("id")) {
            throw new RuntimeException("Failed to create playlist on Spotify.");
        }

        String playlistId = (String) createResponse.get("id");

        // 2. Add tracks to the newly created playlist in chunks of 100
        if (trackIds != null && !trackIds.isEmpty()) {
            final int CHUNK_SIZE = 100;
            for (int i = 0; i < trackIds.size(); i += CHUNK_SIZE) {
                int endIndex = Math.min(i + CHUNK_SIZE, trackIds.size());
                List<String> chunk = trackIds.subList(i, endIndex);

                String addTracksUri = API_BASE + "/playlists/" + playlistId + "/tracks";

                List<String> trackUris = chunk.stream()
                        .map(id -> "spotify:track:" + id)
                        .toList();

                Map<String, Object> addTracksBody = Map.of(
                        "uris", trackUris
                );

                postRequest(addTracksUri, accessToken, addTracksBody, Map.class);
            }
        }

        // 3. Build the final response object
        List<Music> musics = trackIds.stream()
                .map(id -> Music.builder().id(id).name(id).build())
                .toList();

        return Playlist.builder()
                .id(playlistId)
                .name(playlistName)
                .streamingPlatform(StreamingPlatform.SPOTIFY)
                .musics(musics)
                .build();
    }

    private String getCurrentUserId(String accessToken) {
        String uri = API_BASE + "/me";
        Map<String, Object> userProfile = getRequest(uri, accessToken, Map.class);
        if (userProfile == null || !userProfile.containsKey("id")) {
            throw new RuntimeException("Failed to retrieve user ID from Spotify.");
        }
        return (String) userProfile.get("id");
    }

    @Override
    public List<TargetMusicDto> getPlaylistsTracks(String accessToken, String playlistId) {
        String uri = API_BASE + "/playlists/" + playlistId + "/tracks";
        List<TargetMusicDto> musics = new ArrayList<>();

        while (uri != null) {
            Map<String, Object> page = getRequest(uri, accessToken, Map.class);
            List<Map<String, Object>> items = (List<Map<String, Object>>) page.get("items");

            if (items != null) {
                for (Map<String, Object> item : items) {
                    Map<String, Object> track = (Map<String, Object>) item.get("track");

                    if (track == null) {
                        continue;
                    }

                    String id = (String) track.get("id");
                    String name = (String) track.get("name");

                    String artist = null;
                    List<Map<String, Object>> artistsList = (List<Map<String, Object>>) track.get("artists");
                    if (artistsList != null && !artistsList.isEmpty()) {
                        artist = (String) artistsList.get(0).get("name");
                    }

                    String album = null;
                    Map<String, Object> albumMap = (Map<String, Object>) track.get("album");
                    if (albumMap != null && albumMap.containsKey("name")) {
                        album = (String) albumMap.get("name");
                    }

                    String isrc = null;
                    Map<String, Object> externalIds = (Map<String, Object>) track.get("external_ids");
                    if (externalIds != null && externalIds.containsKey("isrc")) {
                        isrc = (String) externalIds.get("isrc");
                    }

                    String duration = null;
                    Integer durationMs = (Integer) track.get("duration_ms");
                    if (durationMs != null) {
                        duration = String.valueOf(durationMs);
                    }

                    String description = null;
                    List<String> keywordsLowList = null;

                    musics.add(new TargetMusicDto(id, name, artist, album, isrc, duration, keywordsLowList));
                }
            }

            uri = (String) page.get("next");
        }

        return musics;
    }

    @Override
    public String findTrackId(String accessToken, TargetMusicDto target, StreamingPlatform fromPlatform) {
        if (target == null || target.name() == null) return null;

        String query = queryBuilder.buildQuery(target, fromPlatform, StreamingPlatform.SPOTIFY);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(API_BASE + "/search")
                .queryParam("q", query)
                .queryParam("type", "track")
                .queryParam("limit", 50);

        String searchUrl = builder.build().toUriString();
        Map<String, Object> response = getRequest(searchUrl, accessToken, Map.class);
        if (response == null || !response.containsKey("tracks")) return null;

        Map<String, Object> tracksObject = (Map<String, Object>) response.get("tracks");
        List<Map<String, Object>> items = (List<Map<String, Object>>) tracksObject.get("items");

        if (items == null) return null;

        List<ResultMusicDto> results = new ArrayList<>();
        for (Map<String, Object> track : items) {
            String id = (String) track.get("id");
            String name = (String) track.get("name");

            String artist = "";
            List<Map<String, Object>> artistsList = (List<Map<String, Object>>) track.get("artists");
            if (artistsList != null && !artistsList.isEmpty()) {
                artist = (String) artistsList.get(0).get("name");
            }

            String album = "";
            Map<String, Object> albumMap = (Map<String, Object>) track.get("album");
            if (albumMap != null && albumMap.containsKey("name")) {
                album = (String) albumMap.get("name");
            }

            String isrc = null;
            Map<String, Object> externalIds = (Map<String, Object>) track.get("external_ids");
            if (externalIds != null && externalIds.containsKey("isrc")) {
                isrc = (String) externalIds.get("isrc");
            }

            String duration = null;
            Integer durationMs = (Integer) track.get("duration_ms");
            if (durationMs != null) {
                duration = String.valueOf(durationMs);
            }

            Music music = Music.builder()
                    .id(id)
                    .name(name)
                    .artist(artist)
                    .album(album)
                    .isrc(isrc)
                    .duration(duration)
                    .build();
            results.add(mapper.toResultMusicDto(music));
        }

        Music bestMatch = matcher.bestMatch(target, results);
        return (bestMatch != null) ? bestMatch.getId() : null;
    }


}
