package com.khundadze.PlaylistConverter.streamingServices.clientSpecificService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.khundadze.PlaylistConverter.models.Music;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.streamingServices.IMusicService;

@Service
public class YouTubeService implements IMusicService {

    private static final Logger logger = LoggerFactory.getLogger(YouTubeService.class);

    @Override
    public List<Playlist> getUsersPlaylists(String accessToken) {
        logger.info("Fetching user playlists from YouTube with accessToken={}", maskToken(accessToken));
        // TODO: implement YouTube Data API call
        return null;
    }

    @Override
    public List<Music> getPlaylistsTracks(String accessToken, Long playlistId) {
        logger.info("Fetching tracks for playlistId={} from YouTube with accessToken={}", playlistId,
                maskToken(accessToken));
        // TODO: implement YouTube Data API call
        return null;
    }

    @Override
    public Playlist createPlaylist(String accessToken, String playlistName, List<String> trackIds) {
        logger.info("Creating YouTube playlist '{}' with tracks={} using accessToken={}", playlistName, trackIds,
                maskToken(accessToken));
        // TODO: implement YouTube Data API call
        return null;
    }

    // Helper to avoid logging full tokens
    private String maskToken(String token) {
        if (token == null || token.length() <= 8)
            return token;
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }
}
