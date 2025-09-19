package com.khundadze.PlaylistConverter.musicTests;

import com.khundadze.PlaylistConverter.dtos.ResultMusicDto;
import com.khundadze.PlaylistConverter.dtos.TargetMusicDto;
import com.khundadze.PlaylistConverter.models.Music;
import com.khundadze.PlaylistConverter.streamingServices.MusicMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MusicMatcherTest {

    private MusicMatcher musicMatcher;

    // --- Test Data Setup ---
    private TargetMusicDto targetMusic;

    @BeforeEach
    void setUp() {
        musicMatcher = new MusicMatcher();

        // The target song we are trying to find matches for.
        targetMusic = new TargetMusicDto(
                "target_id",
                "Bohemian Rhapsody",
                "Queen",
                "A Night at the Opera",
                "GBUM71021234", // Example ISRC
                "355000", // 5:55 in milliseconds
                List.of("bohemian", "rhapsody", "queen")
        );
    }

    @Test
    @DisplayName("Should return null when the list of results is null")
    void bestMatch_whenResultsListIsNull_returnsNull() {
        Music best = musicMatcher.bestMatch(targetMusic, null);
        assertNull(best);
    }

    @Test
    @DisplayName("Should return null when the list of results is empty")
    void bestMatch_whenNoResults_returnsNull() {
        Music best = musicMatcher.bestMatch(targetMusic, Collections.emptyList());
        assertNull(best);
    }

    @Test
    @DisplayName("Should select the perfect match when one exists")
    void bestMatch_whenPerfectMatchExists_returnsPerfectMatch() {
        ResultMusicDto perfectMatch = new ResultMusicDto("perfect_id", "Bohemian Rhapsody", "Queen", "A Night at the Opera", "GBUM71021234", "355000", new HashSet<>(Set.of("queen")));
        ResultMusicDto wrongArtist = new ResultMusicDto("wrong_artist_id", "Bohemian Rhapsody", "Panic! At The Disco", "Some Album", null, "355000", new HashSet<>());

        List<ResultMusicDto> results = List.of(wrongArtist, perfectMatch);
        Music best = musicMatcher.bestMatch(targetMusic, results);

        assertNotNull(best);
        assertEquals("perfect_id", best.getId());
    }

    @Test
    @DisplayName("Should highly prefer a match with the correct ISRC")
    void bestMatch_whenIsrcMatchExists_returnsIsrcMatch() {
        ResultMusicDto isrcMatch = new ResultMusicDto("isrc_id", "Bohemian Rhapsody - 2011 Remaster", "Queen", "Greatest Hits", "GBUM71021234", "357000", new HashSet<>(Set.of("queen")));
        ResultMusicDto strongMatchNoIsrc = new ResultMusicDto("strong_id", "Bohemian Rhapsody", "Queen", "A Night at the Opera", null, "355000", new HashSet<>(Set.of("queen")));

        List<ResultMusicDto> results = List.of(strongMatchNoIsrc, isrcMatch);
        Music best = musicMatcher.bestMatch(targetMusic, results);

        assertNotNull(best);
        assertEquals("isrc_id", best.getId());
    }

    @Test
    @DisplayName("Should penalize and avoid a result with a mismatched artist")
    void bestMatch_whenArtistMismatched_selectsCorrectArtist() {
        ResultMusicDto correctArtist = new ResultMusicDto("correct_artist_id", "Bohemian Rhapsody", "Queen", "Classic Queen", null, "356000", new HashSet<>(Set.of("queen")));
        ResultMusicDto wrongArtist = new ResultMusicDto("wrong_artist_id", "Bohemian Rhapsody", "The Muppets", "The Muppets", null, "355000", new HashSet<>());

        List<ResultMusicDto> results = List.of(wrongArtist, correctArtist);
        Music best = musicMatcher.bestMatch(targetMusic, results);

        assertNotNull(best);
        assertEquals("correct_artist_id", best.getId());
    }

    @Test
    @DisplayName("Should penalize and avoid a result with a mismatched album")
    void bestMatch_whenAlbumMismatched_selectsCorrectAlbum() {
        ResultMusicDto correctAlbum = new ResultMusicDto("correct_album_id", "Bohemian Rhapsody", "Queen", "A Night at the Opera", null, "355000", new HashSet<>(Set.of("queen")));
        ResultMusicDto wrongAlbum = new ResultMusicDto("wrong_album_id", "Bohemian Rhapsody", "Queen", "Made in Heaven", null, "355000", new HashSet<>(Set.of("queen")));

        List<ResultMusicDto> results = List.of(wrongAlbum, correctAlbum);
        Music best = musicMatcher.bestMatch(targetMusic, results);

        assertNotNull(best);
        assertEquals("correct_album_id", best.getId());
    }

    @Test
    @DisplayName("Should prefer a result with a closer duration")
    void bestMatch_whenDurationsVary_selectsClosestDuration() {
        ResultMusicDto closeDuration = new ResultMusicDto("close_duration_id", "Bohemian Rhapsody", "Queen", "A Night at the Opera", null, "358000", new HashSet<>(Set.of("queen"))); // 3s diff
        ResultMusicDto farDuration = new ResultMusicDto("far_duration_id", "Bohemian Rhapsody", "Queen", "A Night at the Opera", null, "255000", new HashSet<>(Set.of("queen")));   // 1m 40s diff

        List<ResultMusicDto> results = List.of(farDuration, closeDuration);
        Music best = musicMatcher.bestMatch(targetMusic, results);

        assertNotNull(best);
        assertEquals("close_duration_id", best.getId());
    }

    @Test
    @DisplayName("Should select exact duration match over a close one")
    void bestMatch_shouldPreferExactDuration() {
        ResultMusicDto exactDuration = new ResultMusicDto("exact_duration_id", "Bohemian Rhapsody", "Queen", "A Night at the Opera", null, "355000", new HashSet<>(Set.of("queen"))); // exact
        ResultMusicDto closeDuration = new ResultMusicDto("close_duration_id", "Bohemian Rhapsody", "Queen", "A Night at the Opera", null, "356000", new HashSet<>(Set.of("queen"))); // 1s diff

        List<ResultMusicDto> results = List.of(closeDuration, exactDuration);
        Music best = musicMatcher.bestMatch(targetMusic, results);

        assertNotNull(best);
        assertEquals("exact_duration_id", best.getId());
    }

    @Test
    @DisplayName("Should find a match when artist is in keywords but not artist field")
    void bestMatch_whenArtistIsInKeywords_findsMatch() {
        ResultMusicDto artistInKeywords = new ResultMusicDto("keyword_id", "Bohemian Rhapsody (Live)", null, null, null, "360000", new HashSet<>(Set.of("queen", "live", "wembley")));
        ResultMusicDto completelyWrong = new ResultMusicDto("wrong_id", "Stairway to Heaven", "Led Zeppelin", null, null, "482000", new HashSet<>(Set.of("led", "zeppelin")));

        List<ResultMusicDto> results = List.of(completelyWrong, artistInKeywords);
        Music best = musicMatcher.bestMatch(targetMusic, results);

        assertNotNull(best);
        assertEquals("keyword_id", best.getId());
    }

    @Test
    @DisplayName("Should handle invalid duration strings gracefully")
    void bestMatch_whenDurationIsInvalid_doesNotCrash() {
        ResultMusicDto validDuration = new ResultMusicDto("valid_id", "Bohemian Rhapsody", "Queen", "A Night at the Opera", null, "355000", new HashSet<>(Set.of("queen")));
        ResultMusicDto invalidDuration = new ResultMusicDto("invalid_id", "Bohemian Rhapsody", "Queen", "A Night at the Opera", null, "not a number", new HashSet<>(Set.of("queen")));
        ResultMusicDto nullDuration = new ResultMusicDto("null_id", "Bohemian Rhapsody", "Queen", "A Night at the Opera", null, null, new HashSet<>(Set.of("queen")));

        List<ResultMusicDto> results = List.of(invalidDuration, nullDuration, validDuration);

        // This test mainly ensures that no exception is thrown.
        Music best = musicMatcher.bestMatch(targetMusic, results);
        assertNotNull(best);
        assertEquals("valid_id", best.getId());
    }

    @Test
    @DisplayName("Should correctly normalize and match titles with extra text")
    void bestMatch_whenTitleHasExtraText_matchesCorrectly() {
        ResultMusicDto normalizedTitle = new ResultMusicDto("norm_id", "Bohemian Rhapsody (Official Video)", "Queen", "A Night at the Opera", null, "355000", new HashSet<>(Set.of("queen")));
        ResultMusicDto otherSong = new ResultMusicDto("other_id", "Another One Bites the Dust", "Queen", "The Game", null, "215000", new HashSet<>(Set.of("queen")));

        List<ResultMusicDto> results = List.of(otherSong, normalizedTitle);
        Music best = musicMatcher.bestMatch(targetMusic, results);

        assertNotNull(best);
        assertEquals("norm_id", best.getId());
    }
}

