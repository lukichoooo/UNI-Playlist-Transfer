package com.khundadze.PlaylistConverter.streamingServices.clientSpecificService;

import org.springframework.stereotype.Service;

import com.khundadze.PlaylistConverter.streamingServices.IMusicService;

import java.util.List;

@Service
public class YouTubeService implements IMusicService {

    @Override
    public String getUsersPlaylists(Long userId, String accessToken) {
        // TODO: implement YouTube Data API call
        return null;
    }

    @Override
    public String getPlaylistsTracks(Long playlistId, String accessToken) {
        // TODO: implement YouTube Data API call
        return null;
    }

    @Override
    public String createPlaylist(Long userId, String accessToken, String playlistName, List<String> trackIds) {
        // TODO: implement YouTube Data API call
        return null;
    }
}
