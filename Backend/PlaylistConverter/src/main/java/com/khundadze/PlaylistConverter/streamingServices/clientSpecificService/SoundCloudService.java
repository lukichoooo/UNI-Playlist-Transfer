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
public class SoundCloudService implements IMusicService {

    private static final String API_BASE = "https://api.soundcloud.com";

    @Override
    public List<PlaylistSearchDto> getUsersPlaylists(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String url = UriComponentsBuilder
                .fromHttpUrl(API_BASE + "/me/playlists")
                .queryParam("oauth_token", accessToken)
                .build().toUriString();

        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, null, List.class);

        List<PlaylistSearchDto> playlists = new ArrayList<>();
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> items = response.getBody();
            for (Map<String, Object> item : items) {
                String id = String.valueOf(item.get("id"));
                String title = (String) item.get("title");
                List<Map<String, Object>> tracksList = (List<Map<String, Object>>) item.get("tracks");
                int itemCount = tracksList != null ? tracksList.size() : 0;

                playlists.add(new PlaylistSearchDto(id, title, itemCount));
            }
        }
        return playlists;
    }

    @Override
    public List<Music> getPlaylistsTracks(String accessToken, String playlistId) {
        RestTemplate restTemplate = new RestTemplate();
        String url = UriComponentsBuilder
                .fromHttpUrl(API_BASE + "/playlists/" + playlistId)
                .queryParam("oauth_token", accessToken)
                .build().toUriString();

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, null, Map.class);

        List<Music> tracks = new ArrayList<>();
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("tracks");
            if (items != null) {
                for (Map<String, Object> track : items) {
                    String id = String.valueOf(track.get("id"));
                    String title = (String) track.get("title");

                    tracks.add(Music.builder()
                            .id(id)
                            .name(title)
                            .build());
                }
            }
        }
        return tracks;
    }

    @Override
    public Playlist createPlaylist(String accessToken, String playlistName, List<String> trackIds) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Step 1: Create playlist
        String createUrl = API_BASE + "/playlists?oauth_token=" + accessToken;
        Map<String, Object> createBody = Map.of(
                "playlist", Map.of(
                        "title", playlistName,
                        "sharing", "private",
                        "tracks", trackIds.stream().map(id -> Map.of("id", id)).toList()
                )
        );
        HttpEntity<Map<String, Object>> createEntity = new HttpEntity<>(createBody, headers);
        ResponseEntity<Map> createResponse = restTemplate.postForEntity(createUrl, createEntity, Map.class);

        Map<String, Object> responseBody = createResponse.getBody();
        if (responseBody == null || !responseBody.containsKey("id")) {
            throw new RuntimeException("Failed to create playlist on SoundCloud");
        }

        String playlistId = String.valueOf(responseBody.get("id"));

        // Step 2: Build Playlist object
        List<Music> musics = trackIds.stream()
                .map(id -> Music.builder().id(id).name(id).build()) // optionally fetch real titles
                .toList();

        return Playlist.builder()
                .id(playlistId)
                .name(playlistName)
                .streamingPlatform(StreamingPlatform.SOUNDCLOUD)
                .musics(musics)
                .build();
    }
}
