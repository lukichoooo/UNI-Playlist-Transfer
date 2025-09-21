package com.khundadze.PlaylistConverter.streamingServices.algorithm.searchQuery;

import com.khundadze.PlaylistConverter.dtos.TargetMusicDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlatformQueryRegistry {

    private final QueryNormalizer normalizer;

    public String buildPlatformQuery(TargetMusicDto target, StreamingPlatform toPlatform) {
        return switch (toPlatform) {
            case YOUTUBE -> generateYoutubeQuery(target);
            case SOUNDCLOUD -> generateSoundcloudQuery(target);
            default -> throw new IllegalArgumentException("Unsupported platform: " + toPlatform);
        };
    }

    private String generateYoutubeQuery(TargetMusicDto target) {
        String trackName = normalizer.normalizeForQuery(target.name());
        return trackName.trim() + " music";
    }

    private String generateSoundcloudQuery(TargetMusicDto target) {
        String artistName = normalizer.normalizeForQuery(target.artist());
        String trackName = normalizer.normalizeForQuery(target.name());

        return (artistName + " " + trackName).trim();
    }


}