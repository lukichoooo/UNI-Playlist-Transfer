// src/main/java/com/khundadze/PlaylistConverter/streamingServices/YouTubeService.java
package com.khundadze.PlaylistConverter.streamingServices;

import org.springframework.stereotype.Service;

@Service
public class YouTubeService implements IMusicService {

    @Override
    public String getUsersPlaylists(Long userId, String accessToken) {
        // TODO: implement YouTube API call
        return null;
    }

    @Override
    public String getPlaylistsTracks(Long playlistId, String accessToken) {
        // TODO: implement YouTube API call
        return null;
    }

    @Override
    public String createPlaylist(Long userId, String accessToken) {
        // TODO: implement YouTube API call
        return null;
    }
}
