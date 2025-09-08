// src/main/java/com/khundadze/PlaylistConverter/streamingServices/SpotifyService.java
package com.khundadze.PlaylistConverter.streamingServices;

import org.springframework.stereotype.Service;

@Service
public class SpotifyService implements IMusicService {

    @Override
    public String getUsersPlaylists(Long userId, String accessToken) {
        // TODO: implement Spotify API call
        return null;
    }

    @Override
    public String getPlaylistsTracks(Long playlistId, String accessToken) {
        // TODO: implement Spotify API call
        return null;
    }

    @Override
    public String createPlaylist(Long userId, String accessToken) {
        // TODO: implement Spotify API call
        return null;
    }
}
