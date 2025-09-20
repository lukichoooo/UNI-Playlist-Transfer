package com.khundadze.PlaylistConverter.streamingServices.clientSpecificService;

import com.khundadze.PlaylistConverter.dtos.PlaylistSearchDto;
import com.khundadze.PlaylistConverter.dtos.ResultMusicDto;
import com.khundadze.PlaylistConverter.dtos.TargetMusicDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.models.Music;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.services.MusicMapper;
import com.khundadze.PlaylistConverter.streamingServices.MusicMatcher;
import com.khundadze.PlaylistConverter.streamingServices.MusicService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class YouTubeService extends MusicService {

    public YouTubeService(
            @Qualifier("youTubeWebClient") WebClient webClient,
            MusicMatcher matcher,
            MusicMapper mapper
    ) {
        super(matcher, mapper, webClient);
    }

    private final String API_BASE = "https://www.googleapis.com/youtube/v3";

    @Override
    public List<PlaylistSearchDto> getUsersPlaylists(String accessToken) {
        String url = UriComponentsBuilder
                .fromHttpUrl(API_BASE + "/playlists")
                .queryParam("part", "snippet,contentDetails")
                .queryParam("mine", "true")
                .queryParam("maxResults", 50)
                .build().toUriString();

        Map<String, Object> response = getRequest(url, accessToken, Map.class);
        List<PlaylistSearchDto> playlists = new ArrayList<>();

        if (response != null) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
            if (items != null) {
                for (Map<String, Object> item : items) {
                    Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
                    Map<String, Object> contentDetails = (Map<String, Object>) item.get("contentDetails");

                    int itemCount = 0;
                    if (contentDetails != null && contentDetails.get("itemCount") != null) {
                        itemCount = Integer.parseInt(contentDetails.get("itemCount").toString());
                    }

                    String id = (String) item.get("id");
                    String title = snippet != null ? (String) snippet.get("title") : null;

                    playlists.add(new PlaylistSearchDto(id, title, itemCount));
                }
            }
        }

        return playlists;
    }


    @Override
    public Playlist createPlaylist(String accessToken, String playlistName, List<String> trackIds) {
        // 1️⃣ Create the YouTube playlist
        String url = API_BASE + "/playlists?part=snippet,status";
        Map<String, Object> body = Map.of(
                "snippet", Map.of("title", playlistName),
                "status", Map.of("privacyStatus", "private")
        );

        Map<String, Object> response = postRequest(url, accessToken, body, Map.class);

        if (response == null || !response.containsKey("id")) {
            throw new RuntimeException("Failed to create playlist on YouTube");
        }

        String playlistId = (String) response.get("id");

        // 2️⃣ Add each track individually
        List<Music> musics = new ArrayList<>();
        for (String videoId : trackIds) {
            Map<String, Object> playlistItemBody = Map.of(
                    "snippet", Map.of(
                            "playlistId", playlistId,
                            "resourceId", Map.of(
                                    "kind", "youtube#video",
                                    "videoId", videoId
                            )
                    )
            );

            Map<String, Object> addResponse = postRequest(
                    API_BASE + "/playlistItems?part=snippet",
                    accessToken,
                    playlistItemBody,
                    Map.class
            );

            System.out.println("Adding video " + videoId); // ✅ Debug

            // Collect Music object (using videoId as a placeholder name)
            musics.add(Music.builder().id(videoId).name(videoId).build());
        }

        // 3️⃣ Build and return Playlist
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
        String pageToken = null;

        do {
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://www.googleapis.com/youtube/v3/playlistItems")
                    .queryParam("part", "snippet,contentDetails")
                    .queryParam("playlistId", playlistId)
                    .queryParam("maxResults", 50)
                    .queryParamIfPresent("pageToken", Optional.ofNullable(pageToken))
                    .toUriString();

            Map<String, Object> body = getRequest(url, accessToken, Map.class);
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

                    System.out.println("Fetched video: " + videoId + " - " + name); // 🔹 debug

                    Music music = Music.builder()
                            .id(videoId)
                            .name(name)
                            .artist(artist)
                            .album("")   // YouTube doesn’t provide album info
                            .isrc("")    // YouTube doesn’t provide ISRC
                            .duration(null) // (Can't fetch without seperate API call)
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
        if (target == null || target.name() == null || target.name().isBlank()) {
            return null;
        }

        String searchUrl = UriComponentsBuilder.fromHttpUrl(API_BASE + "/search")
                .queryParam("part", "snippet")
                .queryParam("type", "video")
                .queryParam("q", target.name())
                .queryParam("order", "viewCount")
                .queryParam("maxResults", 50)
                .toUriString();

        Map<String, Object> searchResponse = getRequest(searchUrl, accessToken, Map.class);
        if (searchResponse == null || searchResponse.get("items") == null || ((List<?>) searchResponse.get("items")).isEmpty()) {
            return null;
        }

        // --- STEP 2: Map the search results to DTOs ---
        List<Map<String, Object>> searchItems = (List<Map<String, Object>>) searchResponse.get("items");
        List<ResultMusicDto> results = new ArrayList<>();
        for (Map<String, Object> item : searchItems) {
            Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
            Map<String, Object> idMap = (Map<String, Object>) item.get("id");

            if (snippet == null || idMap == null || idMap.get("videoId") == null) continue;

            Music music = Music.builder()
                    .id(idMap.get("videoId").toString())
                    .name((String) snippet.get("title"))
                    .artist((String) snippet.get("channelTitle"))
                    .description((String) snippet.get("description"))
                    // Duration, Album, and ISRC are not available from the search endpoint
                    .duration(null)
                    .album(null)
                    .isrc(null)
                    .build();
            results.add(mapper.toResultMusicDto(music));
        }

        if (results.isEmpty()) {
            return null;
        }

        Music bestMatch = matcher.bestMatch(target, results);
        System.out.println("Found song: " + ((bestMatch != null) ? bestMatch.getName() : null));
        return (bestMatch != null) ? bestMatch.getId() : null;
    }


}
