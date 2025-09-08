// src/main/java/com/khundadze/PlaylistConverter/streamingServices/SoundCloudService.java
package com.khundadze.PlaylistConverter.streamingServices;

import org.springframework.stereotype.Service;

@Service
public class SoundCloudService implements IMusicService {

    @Override
    public String getUsersPlaylists(Long userId, String accessToken) {
        // TODO: implement SoundCloud API call
        return null;
    }

    @Override
    public String getPlaylistsTracks(Long playlistId, String accessToken) {
        // TODO: implement SoundCloud API call
        return null;
    }

    @Override
    public String createPlaylist(Long userId, String accessToken) {
        // TODO: implement SoundCloud API call
        return null;
    }
}
