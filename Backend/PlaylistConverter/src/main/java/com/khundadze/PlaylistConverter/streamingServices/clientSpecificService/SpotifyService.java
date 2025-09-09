package com.khundadze.PlaylistConverter.streamingServices.clientSpecificService;

import org.springframework.stereotype.Service;

import com.khundadze.PlaylistConverter.models.Music;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.streamingServices.IMusicService;

import java.util.List;

@Service
public class SpotifyService implements IMusicService {

    @Override
    public List<Playlist> getUsersPlaylists(Long userId, String accessToken) {
        // TODO: implement Spotify API call
        return null;
    }

    @Override
    public List<Music> getPlaylistsTracks(Long playlistId, String accessToken) {
        // TODO: implement Spotify API call
        return null;
    }

    @Override
    public Playlist createPlaylist(Long userId, String accessToken, String playlistName, List<String> trackIds) {
        // TODO: implement Spotify API call
        return null;
    }
}
