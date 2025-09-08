package com.khundadze.PlaylistConverter.streamingServices.clientSpecificService;

import org.springframework.stereotype.Service;

import com.khundadze.PlaylistConverter.streamingServices.IMusicService;

import java.util.List;

@Service
public class DeezerService implements IMusicService {

    @Override
    public String getUsersPlaylists(Long userId, String accessToken) {
        // TODO: implement Deezer API call
        return null;
    }

    @Override
    public String getPlaylistsTracks(Long playlistId, String accessToken) {
        // TODO: implement Deezer API call
        return null;
    }

    @Override
    public String createPlaylist(Long userId, String accessToken, String playlistName, List<String> trackIds) {
        // TODO: implement Deezer API call
        return null;

    }
}
