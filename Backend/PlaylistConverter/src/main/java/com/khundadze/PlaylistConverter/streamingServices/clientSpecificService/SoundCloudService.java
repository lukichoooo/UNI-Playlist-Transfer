package com.khundadze.PlaylistConverter.streamingServices.clientSpecificService;

import com.khundadze.PlaylistConverter.dtos.PlaylistSearchDto;
import com.khundadze.PlaylistConverter.dtos.TargetMusicDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.models.Music;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.services.MusicMapper;
import com.khundadze.PlaylistConverter.streamingServices.MusicMatcher;
import com.khundadze.PlaylistConverter.streamingServices.MusicService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SoundCloudService extends MusicService {

    public SoundCloudService(MusicMatcher matcher, MusicMapper mapper, WebClient webClient) {
        super(matcher, mapper, webClient);
    }

    private final String API_BASE = "https://api.soundcloud.com";

    private HttpHeaders buildAuthHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Override
    public List<PlaylistSearchDto> getUsersPlaylists(String accessToken) {
        List<Map<String, Object>> response = get(API_BASE + "/me/playlists", accessToken, List.class);
        List<PlaylistSearchDto> playlists = new ArrayList<>();

        if (response != null) {
            for (Map<String, Object> item : response) {
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
    public Playlist createPlaylist(String accessToken, String playlistName, List<String> trackIds) {
        String url = API_BASE + "/playlists";
        Map<String, Object> body = Map.of(
                "playlist", Map.of(
                        "title", playlistName,
                        "sharing", "private",
                        "tracks", trackIds.stream().map(id -> Map.of("id", id)).toList()
                )
        );

        Map<String, Object> response = post(url, accessToken, body, Map.class);
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
        Map<String, Object> response = get(API_BASE + "/playlists/" + playlistId, accessToken, Map.class);
        List<TargetMusicDto> tracks = new ArrayList<>();

        if (response != null) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("tracks");
            if (items != null) {
                for (Map<String, Object> track : items) {
                    String id = String.valueOf(track.get("id"));
                    String title = (String) track.get("title");
                    String artist = (String) track.get("user");
                    String album = (String) track.get("playlist");
                    String isrc = (String) track.get("track_code");
                    String duration = (String) track.get("duration");
                    String description = (String) track.get("description");
                    tracks.add(new TargetMusicDto(id, title, null, null, null, null, null));
                }
            }
        }
        return tracks;
    }

    @Override
    public String findTrackId(String accessToken, TargetMusicDto target) {
        if (target == null || target.name() == null) return null;

        String query = UriComponentsBuilder
                .fromHttpUrl(API_BASE + "/tracks")
                .queryParam("q", target.name())
                .queryParam("limit", 10)
                .build().toUriString();

        List<Map<String, Object>> response = get(query, accessToken, List.class);
        if (response == null) return null;

        for (Map<String, Object> track : response) {
            String title = (String) track.get("title");
            String artist = track.get("user") != null
                    ? (String) ((Map<String, Object>) track.get("user")).get("username")
                    : null;

            // Simple matching: title and optionally artist
            if (title != null && title.equalsIgnoreCase(target.name())) {
                if (target.artist() == null || (artist != null && artist.equalsIgnoreCase(target.artist()))) {
                    return String.valueOf(track.get("id"));
                }
            }
        }
        return null; // not found
    }


}
