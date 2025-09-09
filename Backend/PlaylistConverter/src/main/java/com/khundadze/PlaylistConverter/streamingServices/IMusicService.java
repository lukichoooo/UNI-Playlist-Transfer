package com.khundadze.PlaylistConverter.streamingServices;

import java.util.List;

import com.khundadze.PlaylistConverter.models.Music;
import com.khundadze.PlaylistConverter.models.Playlist;

public interface IMusicService {
    List<Playlist> getUsersPlaylists(Long userId, String accessToken);

    List<Music> getPlaylistsTracks(Long playlistId, String accessToken);

    Playlist createPlaylist(Long userId, String accessToken, String playlistName, List<String> trackIds);
}
