package com.khundadze.PlaylistConverter.streamingServices.clientSpecificService;

import com.khundadze.PlaylistConverter.dtos.PlaylistSearchDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.models.Music;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.streamingServices.IMusicService;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SoundCloudService implements IMusicService {

    private static final String API_BASE = "https://api.soundcloud.com";
    private final RestTemplate restTemplate;

    public SoundCloudService() {
        this.restTemplate = new RestTemplate();
    }

    private HttpHeaders buildAuthHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private <T> ResponseEntity<T> get(String url, String token, Class<T> responseType) {
        HttpEntity<Void> entity = new HttpEntity<>(buildAuthHeaders(token));
        return restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
    }

    private <T> ResponseEntity<T> post(String url, String token, Object body, Class<T> responseType) {
        HttpEntity<Object> entity = new HttpEntity<>(body, buildAuthHeaders(token));
        return restTemplate.postForEntity(url, entity, responseType);
    }

    @Override
    public List<PlaylistSearchDto> getUsersPlaylists(String accessToken) {
        ResponseEntity<List> response = get(API_BASE + "/me/playlists", accessToken, List.class);
        List<PlaylistSearchDto> playlists = new ArrayList<>();

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            for (Object obj : response.getBody()) {
                Map<String, Object> item = (Map<String, Object>) obj;
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
        ResponseEntity<Map> response = get(API_BASE + "/playlists/" + playlistId, accessToken, Map.class);
        List<Music> tracks = new ArrayList<>();

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("tracks");
            if (items != null) {
                for (Map<String, Object> track : items) {
                    String id = String.valueOf(track.get("id"));
                    String title = (String) track.get("title");
                    tracks.add(Music.builder().id(id).name(title).build());
                }
            }
        }
        return tracks;
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

        ResponseEntity<Map> response = post(url, accessToken, body, Map.class);
        Map<String, Object> responseBody = response.getBody();

        if (responseBody == null || !responseBody.containsKey("id")) {
            throw new RuntimeException("Failed to create playlist on SoundCloud");
        }

        String playlistId = String.valueOf(responseBody.get("id"));
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
}
