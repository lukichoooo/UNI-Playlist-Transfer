package com.khundadze.PlaylistConverter.algorithmTests;

import com.khundadze.PlaylistConverter.dtos.TargetMusicDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.streamingServices.algorithm.MusicQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class MusicQueryBuilderTest {

    private MusicQueryBuilder musicQueryBuilder;

    @BeforeEach
    void setUp() {
        musicQueryBuilder = new MusicQueryBuilder();
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when TargetMusicDto is null")
    void buildQuery_whenTargetIsNull_throwsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            musicQueryBuilder.buildQuery(null, StreamingPlatform.SPOTIFY);
        });
        assertEquals("Target music DTO and track name cannot be null or blank.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when track name is null")
    void buildQuery_whenTrackNameIsNull_throwsIllegalArgumentException() {
        TargetMusicDto target = new TargetMusicDto("id", null, "artist", "album", "isrc", "180000", Collections.emptyList());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            musicQueryBuilder.buildQuery(target, StreamingPlatform.SPOTIFY);
        });
        assertEquals("Target music DTO and track name cannot be null or blank.", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t"})
    @DisplayName("Should throw IllegalArgumentException when track name is blank")
    void buildQuery_whenTrackNameIsBlank_throwsIllegalArgumentException(String blankName) {
        TargetMusicDto target = new TargetMusicDto("id", blankName, "artist", "album", "isrc", "180000", Collections.emptyList());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            musicQueryBuilder.buildQuery(target, StreamingPlatform.SPOTIFY);
        });
        assertEquals("Target music DTO and track name cannot be null or blank.", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = StreamingPlatform.class, names = {"SPOTIFY", "DEEZER"})
    @DisplayName("Should return ISRC query for supported platforms when ISRC is present")
    void buildQuery_forIsrcSupportedPlatforms_returnsIsrcQuery(StreamingPlatform platform) {
        TargetMusicDto target = new TargetMusicDto("id", "Hello", "Adele", "25", "US-SM1-15-00228", "295000", Collections.emptyList());
        String expectedQuery = "isrc:US-SM1-15-00228";
        String actualQuery = musicQueryBuilder.buildQuery(target, platform);
        assertEquals(expectedQuery, actualQuery);
    }

    @ParameterizedTest
    @EnumSource(value = StreamingPlatform.class, names = {"SPOTIFY", "DEEZER"})
    @DisplayName("Should return text query for supported platforms when ISRC is blank")
    void buildQuery_forIsrcSupportedPlatforms_withBlankIsrc_returnsTextQuery(StreamingPlatform platform) {
        TargetMusicDto target = new TargetMusicDto("id", "Hello", "Adele", "25", "  ", "295000", Collections.emptyList());
        String expectedQuery = "hello adele";
        String actualQuery = musicQueryBuilder.buildQuery(target, platform);
        assertEquals(expectedQuery, actualQuery);
    }

    @ParameterizedTest
    @EnumSource(value = StreamingPlatform.class, names = {"YOUTUBE", "APPLEMUSIC", "SOUNDCLOUD"})
    @DisplayName("Should return text query for unsupported platforms even if ISRC is present")
    void buildQuery_forIsrcUnsupportedPlatforms_returnsTextQuery(StreamingPlatform platform) {
        TargetMusicDto target = new TargetMusicDto("id", "Hello", "Adele", "25", "US-SM1-15-00228", "295000", Collections.emptyList());
        String actualQuery = musicQueryBuilder.buildQuery(target, platform);
        assertFalse(actualQuery.startsWith("isrc:"));
        assertTrue(actualQuery.contains("hello adele"));
    }

    @Test
    @DisplayName("Should append ' audio' keyword for YouTube platform")
    void buildQuery_forYouTube_appendsAudioKeyword() {
        TargetMusicDto target = new TargetMusicDto("id", "Stairway to Heaven", "Led Zeppelin", null, null, "482000", Collections.emptyList());
        String expectedQuery = "stairway to heaven led zeppelin audio";
        String actualQuery = musicQueryBuilder.buildQuery(target, StreamingPlatform.YOUTUBE);
        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    @DisplayName("Should build correct query with only a track name")
    void buildQuery_withOnlyTrackName_returnsNormalizedName() {
        TargetMusicDto target = new TargetMusicDto("id", "Clair de Lune", null, null, null, "300000", Collections.emptyList());
        String expectedQuery = "clair de lune";
        String actualQuery = musicQueryBuilder.buildQuery(target, StreamingPlatform.APPLEMUSIC);
        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    @DisplayName("Should handle special characters and extra spacing during normalization")
    void buildQuery_withSpecialCharsAndSpacing_returnsCleanQuery() {
        TargetMusicDto target = new TargetMusicDto(
                "id",
                "  Don't Stop Me Now - 2011 Mix  ",
                "  Queen ",
                null, null, "209000", Collections.emptyList()
        );
        String expectedQuery = "don't stop me now 2011 mix queen";
        String actualQuery = musicQueryBuilder.buildQuery(target, StreamingPlatform.DEEZER);
        assertEquals(expectedQuery, actualQuery);
    }
}