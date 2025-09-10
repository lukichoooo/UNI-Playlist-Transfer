package com.khundadze.PlaylistConverter.streamingServices.clientSpecificService;

import org.springframework.stereotype.Service;

import com.khundadze.PlaylistConverter.models.Music;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.streamingServices.IMusicService;

import java.util.List;

@Service
public class DeezerService implements IMusicService {

    @Override
    public List<Playlist> getUsersPlaylists(String accessToken) {
        // TODO: implement Deezer API call
        return null;
    }

    @Override
    public List<Music> getPlaylistsTracks(Long playlistId, String accessToken) {
        // TODO: implement Deezer API call
        return null;
    }

    @Override
    public Playlist createPlaylist(String accessToken, String playlistName, List<String> trackIds) {
        // TODO: implement Deezer API call
        return null;

    }
}
