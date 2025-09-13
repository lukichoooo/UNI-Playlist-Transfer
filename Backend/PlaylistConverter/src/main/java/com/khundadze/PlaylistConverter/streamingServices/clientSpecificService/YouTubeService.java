package com.khundadze.PlaylistConverter.streamingServices.clientSpecificService;

import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.models.Music;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.streamingServices.IMusicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class YouTubeService implements IMusicService { // TODO:

    private static final Logger logger = LoggerFactory.getLogger(YouTubeService.class);

    @Override
    public List<Playlist> getUsersPlaylists(String accessToken) {
        logger.info("Fetching user playlists from YouTube with accessToken");
        RestTemplate restTemplate = new RestTemplate();

        String url = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/playlists").queryParam("part", "snippet").queryParam("mine", "true").build().toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        List<Playlist> playlists = new ArrayList<>();
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");
            if (items != null) {
                for (Map<String, Object> item : items) {
                    Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
                    Playlist p = Playlist.builder()
                            .id((String) item.get("id"))
                            .name((String) snippet.get("title"))
                            .streamingPlatform(StreamingPlatform.YOUTUBE)
                            .build();

                }
            }
        }
        return playlists;
    }


    @Override
    public List<Music> getPlaylistsTracks(String accessToken, String playlistId) {
        logger.info("Fetching tracks for playlistId={} from YouTube with accessToken", playlistId);

        List<Music> tracks = new ArrayList<>();
        String url = "https://www.googleapis.com/youtube/v3/playlistItems"
                + "?part=snippet,contentDetails"
                + "&playlistId=" + playlistId
                + "&maxResults=50";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");

        for (Map<String, Object> item : items) {
            Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
            String videoId = ((Map<String, Object>) snippet.get("resourceId")).get("videoId").toString();
            String name = snippet.get("title").toString();

            Music music = Music.builder()
                    .id(videoId)
                    .name(name)
                    .build();

            tracks.add(music);
        }

        return tracks;
    }


    @Override
    public Playlist createPlaylist(String accessToken, String playlistName, List<String> trackIds) {
        logger.info("Creating YouTube playlist '{}' with tracks={}", playlistName, trackIds);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Step 1: Create the playlist
        String createUrl = "https://www.googleapis.com/youtube/v3/playlists?part=snippet,status";
        Map<String, Object> createBody = Map.of(
                "snippet", Map.of("title", playlistName, "description", "Created via PlaylistConverter"),
                "status", Map.of("privacyStatus", "private")
        );

        HttpEntity<Map<String, Object>> createEntity = new HttpEntity<>(createBody, headers);
        ResponseEntity<Map> createResponse = restTemplate.postForEntity(createUrl, createEntity, Map.class);

        Map<String, Object> responseBody = createResponse.getBody();
        if (responseBody == null || !responseBody.containsKey("id")) {
            throw new RuntimeException("Failed to create playlist on YouTube");
        }

        String playlistId = (String) responseBody.get("id");

        // Step 2: Add tracks to the playlist
        for (String videoId : trackIds) {
            String addUrl = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet";
            Map<String, Object> addBody = Map.of(
                    "snippet", Map.of(
                            "playlistId", playlistId,
                            "resourceId", Map.of("kind", "youtube#video", "videoId", videoId)
                    )
            );

            HttpEntity<Map<String, Object>> addEntity = new HttpEntity<>(addBody, headers);
            restTemplate.postForEntity(addUrl, addEntity, Map.class);
        }

        // Step 3: Populate Playlist object with tracks
        List<Music> musics = new ArrayList<>();
        for (String videoId : trackIds) {
            // Optionally, fetch full details from YouTube if you want more than just ID
            Music music = Music.builder()
                    .id(videoId)
                    .name("") // optionally fetch title from YouTube Data API
                    .build();
            musics.add(music);
        }

        return Playlist.builder()
                .id(playlistId)
                .name(playlistName)
                .streamingPlatform(StreamingPlatform.YOUTUBE)
                .musics(trackIds.stream()
                        .map(id -> Music.builder().id(id).name(id).build())
                        .toList())
                .build();

    }


}
