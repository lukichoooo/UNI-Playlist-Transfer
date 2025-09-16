package com.khundadze.PlaylistConverter.streamingServices;

import com.khundadze.PlaylistConverter.dtos.PlaylistSearchDto;
import com.khundadze.PlaylistConverter.models.Music;
import com.khundadze.PlaylistConverter.models.Playlist;

import java.util.List;

public interface IMusicService {
    List<PlaylistSearchDto> getUsersPlaylists(String accessToken);

    List<Music> getPlaylistsTracks(String accessToken, String playlistId);

    Playlist createPlaylist(String accessToken, String playlistName, List<String> trackIds);
}
