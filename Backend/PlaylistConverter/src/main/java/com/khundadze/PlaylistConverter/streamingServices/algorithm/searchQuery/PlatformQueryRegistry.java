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
            case SPOTIFY -> generateSpotifyQuery(target);
            default -> throw new IllegalArgumentException("Unsupported platform: " + toPlatform);
        };
    }

    private String generateYoutubeQuery(TargetMusicDto target) {
        String artistName = normalizer.normalizeForQuery(target.artist());
        String trackName = normalizer.normalizeForQuery(target.name());
        return (trackName + " " + artistName).trim() + " music";
    }

    private String generateSoundcloudQuery(TargetMusicDto target) {
        String artistName = normalizer.normalizeForQuery(target.artist());
        String trackName = normalizer.normalizeForQuery(target.name());

        return (artistName + " " + trackName).trim();
    }

    private String generateSpotifyQuery(TargetMusicDto target) {
        String trackName = normalizer.normalizeForQuery(target.name());
        return trackName.trim();
    }

    private String generateDeezerQuery(TargetMusicDto target) {
        String artistName = normalizer.normalizeForQuery(target.artist());
        String trackName = normalizer.normalizeForQuery(target.name());
        String albumName = normalizer.normalizeForQuery(target.album());

        // Use advanced search if artist and/or album are present
        if (!artistName.isEmpty() && !trackName.isEmpty()) {
            return "artist:\"" + artistName + "\" track:\"" + trackName + "\"";
        } else if (!trackName.isEmpty()) {
            return "track:\"" + trackName + "\"";
        }
        return trackName.trim();
    }
}