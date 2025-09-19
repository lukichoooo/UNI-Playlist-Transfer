package com.khundadze.PlaylistConverter.streamingServices.clientSpecificService;

import com.khundadze.PlaylistConverter.dtos.PlaylistSearchDto;
import com.khundadze.PlaylistConverter.dtos.ResultMusicDto;
import com.khundadze.PlaylistConverter.dtos.TargetMusicDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.models.Music;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.services.MusicMapper;
import com.khundadze.PlaylistConverter.streamingServices.MusicService;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class YouTubemusicService extends MusicService {

    private final MusicMapper mapper;

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


    @Override
    public List<TargetMusicDto> getPlaylistsTracks(String accessToken, String playlistId) {
        List<TargetMusicDto> tracks = new ArrayList<>();
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
                    String artist = snippet.getOrDefault("videoOwnerChannelTitle", "").toString();
                    String description = snippet.getOrDefault("description", "").toString();
                    if (description.length() > 450) {
                        String prefix = description.substring(0, 300);
                        String suffix = description.substring(description.length() - 150);
                        description = prefix + " " + suffix;
                    }
                    List<String> descriptionKeywords = Arrays.stream(description.split("\\s+"))
                            .filter(w -> !w.isBlank() && w.length() > 2)
                            .map(String::toLowerCase)
                            .toList();

                    Music music = Music.builder()
                            .id(videoId)
                            .name(name)
                            .artist(artist)
                            .description(description)
                            .build();

                    tracks.add(mapper.toTargetMusicDto(music));
                }
            }

            pageToken = (String) body.get("nextPageToken");
        } while (pageToken != null);

        return tracks;
    }

    @Override
    public String findTrackId(String accessToken, TargetMusicDto target) {
        if (target == null || target.name() == null) return null;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Build query: include artist if available
        String query = target.name();
        if (target.artist() != null && !target.artist().isBlank()) {
            query += " " + target.artist();
        }

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl("https://www.googleapis.com/youtube/v3/search")
                .queryParam("part", "snippet")
                .queryParam("q", query)
                .queryParam("type", "video")
                .queryParam("maxResults", 10);

        ResponseEntity<Map> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> body = response.getBody();
        if (body == null) return null;

        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        if (items == null || items.isEmpty()) return null;

        List<ResultMusicDto> results = new ArrayList<>();
        for (Map<String, Object> item : items) {
            Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
            if (snippet == null) continue;

            String videoId = ((Map<String, Object>) item.get("id")).get("videoId").toString();
            String title = (String) snippet.get("title");
            String channel = (String) snippet.get("channelTitle");
            String description = (String) snippet.get("description");

            Music music = Music.builder()
                    .id(videoId)
                    .name(title)
                    .artist(channel)
                    .description(description)
                    .build();

            results.add(mapper.toResultMusicDto(music));
        }

        return bestMatch(target, results).getId();
    }


}
