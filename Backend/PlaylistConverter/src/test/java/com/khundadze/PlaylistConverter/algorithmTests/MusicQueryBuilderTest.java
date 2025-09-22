package com.khundadze.PlaylistConverter.algorithmTests;

import com.khundadze.PlaylistConverter.dtos.TargetMusicDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.streamingServices.algorithm.searchQuery.MusicQueryBuilder;
import com.khundadze.PlaylistConverter.streamingServices.algorithm.searchQuery.PlatformQueryRegistry;
import com.khundadze.PlaylistConverter.streamingServices.algorithm.searchQuery.QueryNormalizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MusicQueryBuilderTest {

    @Mock
    private QueryNormalizer normalizer;
    @Mock
    private PlatformQueryRegistry queryRegistry;

    @InjectMocks
    private MusicQueryBuilder musicQueryBuilder;

    @Test
    @DisplayName("Should throw IllegalArgumentException when TargetMusicDto is null")
    void buildQuery_whenTargetIsNull_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                musicQueryBuilder.buildQuery(null, StreamingPlatform.SPOTIFY, StreamingPlatform.YOUTUBE)
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when track name is null")
    void buildQuery_whenTrackNameIsNull_throwsIllegalArgumentException() {
        TargetMusicDto target = new TargetMusicDto("id", null, "artist", "album", null, "180000", null);
        assertThrows(IllegalArgumentException.class, () ->
                musicQueryBuilder.buildQuery(target, StreamingPlatform.SPOTIFY, StreamingPlatform.YOUTUBE)
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when track name is blank")
    void buildQuery_whenTrackNameIsBlank_throwsIllegalArgumentException() {
        TargetMusicDto target = new TargetMusicDto("id", "   ", "artist", "album", null, "180000", null);
        assertThrows(IllegalArgumentException.class, () ->
                musicQueryBuilder.buildQuery(target, StreamingPlatform.SPOTIFY, StreamingPlatform.YOUTUBE)
        );
    }

    @Test
    @DisplayName("Should use PlatformQueryRegistry for trustworthy 'from' platform (Spotify)")
    void buildQuery_trustworthyPlatform_usesRegistry() {
        // Given a trustworthy source platform
        StreamingPlatform fromPlatform = StreamingPlatform.SPOTIFY;
        StreamingPlatform toPlatform = StreamingPlatform.YOUTUBE;
        TargetMusicDto target = new TargetMusicDto("id", "Hello", "Adele", "25", null, "295000", null);

        // Mock the behavior of the query registry
        String expectedQuery = "hello adele youtube query";
        when(queryRegistry.buildPlatformQuery(target, toPlatform)).thenReturn(expectedQuery);

        // When the query is built
        String actualQuery = musicQueryBuilder.buildQuery(target, fromPlatform, toPlatform);

        // Then it should use the registry and not the normalizer
        assertEquals(expectedQuery, actualQuery);
        verify(queryRegistry, times(1)).buildPlatformQuery(target, toPlatform);
        verifyNoInteractions(normalizer);
    }

    @Test
    @DisplayName("Should use PlatformQueryRegistry for trustworthy 'from' platform (Deezer)")
    void buildQuery_trustworthyPlatform_usesRegistry_deezer() {
        // Given a trustworthy source platform
        StreamingPlatform fromPlatform = StreamingPlatform.DEEZER;
        StreamingPlatform toPlatform = StreamingPlatform.YOUTUBE;
        TargetMusicDto target = new TargetMusicDto("id", "Hello", "Adele", "25", null, "295000", null);

        // Mock the behavior of the query registry
        String expectedQuery = "hello adele youtube query";
        when(queryRegistry.buildPlatformQuery(target, toPlatform)).thenReturn(expectedQuery);

        // When the query is built
        String actualQuery = musicQueryBuilder.buildQuery(target, fromPlatform, toPlatform);

        // Then it should use the registry and not the normalizer
        assertEquals(expectedQuery, actualQuery);
        verify(queryRegistry, times(1)).buildPlatformQuery(target, toPlatform);
        verifyNoInteractions(normalizer);
    }

    @Test
    @DisplayName("Should use QueryNormalizer for non-trustworthy 'from' platform (YouTube)")
    void buildQuery_untrustworthyPlatform_usesNormalizer() {
        // Given a non-trustworthy source platform
        StreamingPlatform fromPlatform = StreamingPlatform.YOUTUBE;
        StreamingPlatform toPlatform = StreamingPlatform.SPOTIFY;
        TargetMusicDto target = new TargetMusicDto("id", "Some Song (Official Music Video)", "An Artist", null, null, "180000", null);

        // Mock the behavior of the normalizer
        String normalizedQuery = "some song an artist";
        when(normalizer.normalizeForQuery(target.name())).thenReturn(normalizedQuery);

        // When the query is built
        String actualQuery = musicQueryBuilder.buildQuery(target, fromPlatform, toPlatform);

        // Then it should use the normalizer and not the registry
        assertEquals(normalizedQuery, actualQuery);
        verify(normalizer, times(1)).normalizeForQuery(target.name());
        verifyNoInteractions(queryRegistry);
    }
}
