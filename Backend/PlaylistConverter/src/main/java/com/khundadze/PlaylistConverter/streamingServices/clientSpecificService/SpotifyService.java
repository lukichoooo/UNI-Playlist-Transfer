package com.khundadze.PlaylistConverter.streamingServices.clientSpecificService;

import com.khundadze.PlaylistConverter.dtos.PlaylistSearchDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.models.Music;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.streamingServices.IMusicService;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SpotifyService implements IMusicService {

    private static final String SPOTIFY_API_BASE = "https://api.spotify.com/v1";

    @Override
    public List<PlaylistSearchDto> getUsersPlaylists(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String url = UriComponentsBuilder.fromHttpUrl(SPOTIFY_API_BASE + "/me/playlists")
                .queryParam("limit", 50)
                .build().toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        List<PlaylistSearchDto> playlists = new ArrayList<>();
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");
            if (items != null) {
                for (Map<String, Object> item : items) {
                    String id = (String) item.get("id");
                    String name = (String) item.get("name");
                    int trackCount = ((Map<String, Object>) item.get("tracks")).get("total") != null
                            ? ((Number) ((Map<String, Object>) item.get("tracks")).get("total")).intValue()
                            : 0;

                    playlists.add(new PlaylistSearchDto(id, name, trackCount));
                }
            }
        }

        return playlists;
    }

    @Override
    public List<Music> getPlaylistsTracks(String accessToken, String playlistId) {
        List<Music> tracks = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = SPOTIFY_API_BASE + "/playlists/" + playlistId + "/tracks?limit=100";
        String nextUrl = url;

        while (nextUrl != null) {
            ResponseEntity<Map> response = restTemplate.exchange(nextUrl, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null) break;

            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
            if (items != null) {
                for (Map<String, Object> item : items) {
                    Map<String, Object> trackMap = (Map<String, Object>) item.get("track");
                    if (trackMap == null) continue;

                    String id = (String) trackMap.get("id");
                    String name = (String) trackMap.get("name");

                    tracks.add(Music.builder().id(id).name(name).build());
                }
            }

            nextUrl = (String) body.get("next");
        }

        return tracks;
    }

    @Override
    public Playlist createPlaylist(String accessToken, String playlistName, List<String> trackIds) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Step 1: Get current user ID
        ResponseEntity<Map> userResponse = restTemplate.exchange(
                SPOTIFY_API_BASE + "/me", HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        String userId = (String) userResponse.getBody().get("id");

        // Step 2: Create playlist
        String createUrl = SPOTIFY_API_BASE + "/users/" + userId + "/playlists";
        Map<String, Object> createBody = Map.of(
                "name", playlistName,
                "description", "Created via PlaylistConverter",
                "public", false
        );

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(createUrl, new HttpEntity<>(createBody, headers), Map.class);
        String playlistId = (String) createResponse.getBody().get("id");

        // Step 3: Add tracks in batches of 100
        for (int i = 0; i < trackIds.size(); i += 100) {
            List<String> batch = trackIds.subList(i, Math.min(i + 100, trackIds.size()));
            String addUrl = SPOTIFY_API_BASE + "/playlists/" + playlistId + "/tracks";
            Map<String, Object> addBody = Map.of("uris", batch.stream().map(id -> "spotify:track:" + id).toList());
            restTemplate.postForEntity(addUrl, new HttpEntity<>(addBody, headers), Map.class);
        }

        // Step 4: Build Playlist object
        List<Music> musics = trackIds.stream().map(id -> Music.builder().id(id).name(id).build()).toList();

        return Playlist.builder()
                .id(playlistId)
                .name(playlistName)
                .streamingPlatform(StreamingPlatform.SPOTIFY)
                .musics(musics)
                .build();
    }
}
