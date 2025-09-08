// src/main/java/com/khundadze/PlaylistConverter/streamingServices/IMusicService.java
package com.khundadze.PlaylistConverter.streamingServices;

public interface IMusicService {
    String getUsersPlaylists(Long userId, String accessToken);

    String getPlaylistsTracks(Long playlistId, String accessToken);

    String createPlaylist(Long userId, String accessToken);
}
