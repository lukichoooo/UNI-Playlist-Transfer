package com.khundadze.PlaylistConverter.streamingServices.algorithm;

import com.khundadze.PlaylistConverter.dtos.TargetMusicDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class MusicQueryBuilder {

    private static final Set<StreamingPlatform> ISRC_SUPPORTED_PLATFORMS = Set.of(
            StreamingPlatform.SPOTIFY,
            StreamingPlatform.DEEZER
    );
    private static final String YOUTUBE_AUDIO_KEYWORD = " audio";

    public String buildQuery(TargetMusicDto target, StreamingPlatform toPlatform) {
        if (target == null || target.name() == null || target.name().isBlank()) {
            throw new IllegalArgumentException("Target music DTO and track name cannot be null or blank.");
        }

        Optional<String> isrcQuery = buildIsrcQuery(target.isrc(), toPlatform);
        if (isrcQuery.isPresent()) {
            return isrcQuery.get();
        }

        return buildTextQuery(target, toPlatform);
    }

    private Optional<String> buildIsrcQuery(String isrc, StreamingPlatform platform) {
        if (isrc != null && !isrc.isBlank() && ISRC_SUPPORTED_PLATFORMS.contains(platform)) {
            return Optional.of("isrc:" + isrc);
        }
        return Optional.empty();
    }

    private String buildTextQuery(TargetMusicDto target, StreamingPlatform platform) {
        StringBuilder queryBuilder = new StringBuilder();

        String normalizedName = normalizeForQuery(target.name());
        queryBuilder.append(normalizedName);

        if (target.artist() != null && !target.artist().isBlank()) {
            queryBuilder.append(" ").append(normalizeForQuery(target.artist()));
        }

        return applyPlatformOptimizations(queryBuilder, platform);
    }

    private String applyPlatformOptimizations(StringBuilder query, StreamingPlatform platform) {
        if (platform == StreamingPlatform.YOUTUBE) {
            query.append(YOUTUBE_AUDIO_KEYWORD);
        }
        return query.toString();
    }

    private String normalizeForQuery(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        return input.toLowerCase()
                .replaceAll("\\(.*?\\)", "")
                .replaceAll("\\[.*?\\]", "")
                .replaceAll("(feat\\.|ft\\.|official video|remastered|\\slive$)", "")
                .replaceAll("[^a-z0-9' ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}