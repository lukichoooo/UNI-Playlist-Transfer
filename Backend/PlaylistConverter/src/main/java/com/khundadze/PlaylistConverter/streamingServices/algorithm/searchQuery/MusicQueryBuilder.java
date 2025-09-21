package com.khundadze.PlaylistConverter.streamingServices.algorithm.searchQuery;

import com.khundadze.PlaylistConverter.dtos.TargetMusicDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class MusicQueryBuilder {

    private final QueryNormalizer normalizer;
    private final PlatformQueryRegistry queryRegistry;

    private static final Set<StreamingPlatform> TRUSTWORTHY_INFO_PLATFORMS = Set.of(
            StreamingPlatform.SPOTIFY,
            StreamingPlatform.DEEZER
    );

    public String buildQuery(TargetMusicDto target, StreamingPlatform fromPlatform, StreamingPlatform toPlatform) {
        if (target == null || target.name() == null || target.name().isBlank()) {
            throw new IllegalArgumentException("Target music DTO and track name cannot be null or blank.");
        }

        if (TRUSTWORTHY_INFO_PLATFORMS.contains(fromPlatform))
            return queryRegistry.buildPlatformQuery(target, toPlatform);

        return normalizer.normalizeForQuery(target.name());
    }
}