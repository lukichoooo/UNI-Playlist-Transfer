package com.khundadze.PlaylistConverter.streamingServices;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.exceptions.UnknownStreamingPlatformException;
import com.khundadze.PlaylistConverter.streamingServices.clientSpecificService.SoundCloudService;
import com.khundadze.PlaylistConverter.streamingServices.clientSpecificService.SpotifyService;
import com.khundadze.PlaylistConverter.streamingServices.clientSpecificService.YouTubeService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StreamingServiceRegistry { // TODO: add all streaming platforms services

    private final SpotifyService spotifyService;
    private final YouTubeService youTubeService;
    private final SoundCloudService soundCloudService;

    private final Map<StreamingPlatform, IMusicService> serviceMap;

    public StreamingServiceRegistry(SpotifyService spotifyService,
            YouTubeService youTubeService,
            SoundCloudService soundCloudService) {
        this.spotifyService = spotifyService;
        this.youTubeService = youTubeService;
        this.soundCloudService = soundCloudService;

        this.serviceMap = Map.of(
                StreamingPlatform.SPOTIFY, spotifyService,
                StreamingPlatform.YOUTUBE, youTubeService,
                StreamingPlatform.SOUNDCLOUD, soundCloudService);
    }

    public IMusicService getService(StreamingPlatform platform) {
        IMusicService svc = serviceMap.get(platform);
        if (svc == null) {
            throw new UnknownStreamingPlatformException("Unknown streaming platform: " + platform);
        }
        return svc;
    }
}
