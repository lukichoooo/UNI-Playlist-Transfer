package com.khundadze.PlaylistConverter.streamingServices.clientSpecificService;

import com.khundadze.PlaylistConverter.models.Music;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.streamingServices.IMusicService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpotifyService implements IMusicService {

    @Override
    public List<Playlist> getUsersPlaylists(String accessToken) {
        // TODO: implement Spotify API call
        return null;
    }

    @Override
    public List<Music> getPlaylistsTracks(String accessToken, String playlistId) {
        // TODO: implement Spotify API call
        return null;
    }

    @Override
    public Playlist createPlaylist(String accessToken, String playlistName, List<String> trackIds) {
        // TODO: implement Spotify API call
        return null;
    }
}
