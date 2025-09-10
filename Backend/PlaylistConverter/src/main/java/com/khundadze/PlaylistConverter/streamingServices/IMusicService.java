package com.khundadze.PlaylistConverter.streamingServices;

import java.util.List;

import com.khundadze.PlaylistConverter.models.Music;
import com.khundadze.PlaylistConverter.models.Playlist;

public interface IMusicService {
    List<Playlist> getUsersPlaylists(String accessToken);

    List<Music> getPlaylistsTracks(String accessToken, Long playlistId);

    Playlist createPlaylist(String accessToken, String playlistName, List<String> trackIds);
}
