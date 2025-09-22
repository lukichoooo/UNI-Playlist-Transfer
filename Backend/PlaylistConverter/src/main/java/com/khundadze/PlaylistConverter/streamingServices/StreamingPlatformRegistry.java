package com.khundadze.PlaylistConverter.streamingServices;

import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.exceptions.UnknownStreamingPlatformException;
import com.khundadze.PlaylistConverter.streamingServices.clientSpecificService.*;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StreamingPlatformRegistry {

    private final SpotifyService spotifyService;
    private final YouTubeService youTubeService;
    private final YouTubemusicService youTubemusicService;
    private final SoundCloudService soundCloudService;
    private final DeezerService deezerService;


    private final Map<StreamingPlatform, MusicService> serviceMap;

    public StreamingPlatformRegistry(SpotifyService spotifyService,
                                     YouTubeService youTubeService,
                                     SoundCloudService soundCloudService,
                                     YouTubemusicService youTubemusicService,
                                     DeezerService deezerService) {
        this.spotifyService = spotifyService;
        this.youTubeService = youTubeService;
        this.soundCloudService = soundCloudService;
        this.youTubemusicService = youTubemusicService;
        this.deezerService = deezerService;

        this.serviceMap = Map.of(
                StreamingPlatform.SPOTIFY, spotifyService,
                StreamingPlatform.YOUTUBE, youTubeService,
                StreamingPlatform.SOUNDCLOUD, soundCloudService,
                StreamingPlatform.YOUTUBEMUSIC, youTubemusicService,
                StreamingPlatform.DEEZER, deezerService);

    }

    public MusicService getService(StreamingPlatform platform) {
        MusicService svc = serviceMap.get(platform);
        if (svc == null) {
            throw new UnknownStreamingPlatformException("Unknown streaming platform: " + platform);
        }
        return svc;
    }
}
