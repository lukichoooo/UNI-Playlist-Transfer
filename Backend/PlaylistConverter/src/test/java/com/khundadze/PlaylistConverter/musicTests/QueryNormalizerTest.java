package com.khundadze.PlaylistConverter.musicTests;

import com.khundadze.PlaylistConverter.streamingServices.algorithm.searchQuery.QueryNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QueryNormalizerTest {

    private QueryNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new QueryNormalizer();
    }

    @Test
    @DisplayName("Should remove common terms like 'official music video'")
    void normalizeForQuery_removesCommonTerms() {
        String input = "Artist - Song (Official Music Video)";
        String expected = "artist - song";
        assertEquals(expected, normalizer.normalizeForQuery(input));
    }

    @Test
    @DisplayName("Should remove bracketed content")
    void normalizeForQuery_removesBracketedContent() {
        String input = "Song Name [Remix] (Live at the Arena)";
        String expected = "song name";
        assertEquals(expected, normalizer.normalizeForQuery(input));
    }

    @Test
    @DisplayName("Should handle featuring variants")
    void normalizeForQuery_removesFeaturing() {
        String input = "Song Title ft. Another Artist";
        String expected = "song title";
        assertEquals(expected, normalizer.normalizeForQuery(input));

        input = "Song Title feat. Another Artist";
        expected = "song title";
        assertEquals(expected, normalizer.normalizeForQuery(input));

        input = "Song Title (Feat Another Artist)";
        expected = "song title";
        assertEquals(expected, normalizer.normalizeForQuery(input));
    }

    @Test
    @DisplayName("Should handle multiple normalizations in one string")
    void normalizeForQuery_handlesMultipleRemovals() {
        String input = "Song Title (Official Video) [Remastered] ft. Another Artist";
        String expected = "song title";
        assertEquals(expected, normalizer.normalizeForQuery(input));
    }

    @Test
    @DisplayName("Should normalize multiple whitespace characters")
    void normalizeForQuery_normalizesWhitespace() {
        String input = "  Song    Name  (Official Video)  ";
        String expected = "song name";
        assertEquals(expected, normalizer.normalizeForQuery(input));
    }

    @Test
    @DisplayName("Should return empty string for null input")
    void normalizeForQuery_returnsEmptyStringForNull() {
        assertEquals("", normalizer.normalizeForQuery(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t"})
    @DisplayName("Should return empty string for blank input")
    void normalizeForQuery_returnsEmptyStringForBlank(String blankInput) {
        assertEquals("", normalizer.normalizeForQuery(blankInput));
    }

    @Test
    @DisplayName("Should handle a clean input string with no special terms")
    void normalizeForQuery_handlesCleanInput() {
        String input = "A Clean Song Title";
        String expected = "a clean song title";
        assertEquals(expected, normalizer.normalizeForQuery(input));
    }
}
