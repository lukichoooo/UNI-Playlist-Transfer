package com.khundadze.PlaylistConverter.streamingServices.clientSpecificService;

import org.springframework.stereotype.Service;

import com.khundadze.PlaylistConverter.models.Music;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.streamingServices.IMusicService;

import java.util.List;

@Service
public class YouTubeService implements IMusicService {

    @Override
    public List<Playlist> getUsersPlaylists(String accessToken) {
        // TODO: implement YouTube Data API call
        return null;
    }

    @Override
    public List<Music> getPlaylistsTracks(Long playlistId, String accessToken) {
        // TODO: implement YouTube Data API call
        return null;
    }

    @Override
    public Playlist createPlaylist(String accessToken, String playlistName, List<String> trackIds) {
        // TODO: implement YouTube Data API call
        return null;
    }
}
