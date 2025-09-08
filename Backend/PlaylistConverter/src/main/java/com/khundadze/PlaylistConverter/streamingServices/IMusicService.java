package com.khundadze.PlaylistConverter.streamingServices;

import java.util.List;

public interface IMusicService {
    String getUsersPlaylists(Long userId, String accessToken);

    String getPlaylistsTracks(Long playlistId, String accessToken);

    String createPlaylist(Long userId, String accessToken, String playlistName, List<String> trackIds);
}
