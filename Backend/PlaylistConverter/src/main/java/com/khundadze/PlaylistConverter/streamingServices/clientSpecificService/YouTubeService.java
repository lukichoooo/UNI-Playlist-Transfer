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
public class YouTubeService implements IMusicService { // TODO:

    @Override
    public List<PlaylistSearchDto> getUsersPlaylists(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        // YouTube API URL
        String url = UriComponentsBuilder
                .fromHttpUrl("https://www.googleapis.com/youtube/v3/playlists")
                .queryParam("part", "snippet,contentDetails")
                .queryParam("mine", "true")
                .queryParam("maxResults", 50) // optional, max per page
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
                    Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
                    Map<String, Object> contentDetails = (Map<String, Object>) item.get("contentDetails");

                    int itemCount = 0;
                    if (contentDetails != null && contentDetails.get("itemCount") != null) {
                        itemCount = ((Number) contentDetails.get("itemCount")).intValue();
                    }

                    PlaylistSearchDto dto = new PlaylistSearchDto(
                            (String) item.get("id"),
                            (String) snippet.get("title"),
                            itemCount
                    );


                    playlists.add(dto);
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

        String pageToken = null;
        do {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl("https://www.googleapis.com/youtube/v3/playlistItems")
                    .queryParam("part", "snippet,contentDetails")
                    .queryParam("playlistId", playlistId)
                    .queryParam("maxResults", 50);
            if (pageToken != null) builder.queryParam("pageToken", pageToken);

            ResponseEntity<Map> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body == null) break;

            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
            if (items != null) {
                for (Map<String, Object> item : items) {
                    Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
                    if (snippet == null) continue;

                    Map<String, Object> resourceId = (Map<String, Object>) snippet.get("resourceId");
                    if (resourceId == null) continue;

                    String videoId = resourceId.get("videoId").toString();
                    String name = snippet.get("title").toString();

                    Music music = Music.builder()
                            .id(videoId)
                            .name(name)
                            .build();

                    tracks.add(music);
                }
            }

            pageToken = (String) body.get("nextPageToken");
        } while (pageToken != null);

        return tracks;
    }


    @Override
    public Playlist createPlaylist(String accessToken, String playlistName, List<String> trackIds) {
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

        // Step 3: Build Playlist object
        List<Music> musics = trackIds.stream()
                .map(id -> Music.builder().id(id).name(id).build()) // or fetch real title if needed
                .toList();

        return Playlist.builder()
                .id(playlistId)
                .name(playlistName)
                .streamingPlatform(StreamingPlatform.YOUTUBE)
                .musics(musics)
                .build();
    }


}
