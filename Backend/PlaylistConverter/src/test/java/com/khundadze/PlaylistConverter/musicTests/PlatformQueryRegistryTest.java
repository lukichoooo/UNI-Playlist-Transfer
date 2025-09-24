package com.khundadze.PlaylistConverter.musicTests;

import com.khundadze.PlaylistConverter.dtos.TargetMusicDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.streamingServices.algorithm.searchQuery.PlatformQueryRegistry;
import com.khundadze.PlaylistConverter.streamingServices.algorithm.searchQuery.QueryNormalizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatformQueryRegistryTest {

    @Mock
    private QueryNormalizer normalizer;

    @InjectMocks
    private PlatformQueryRegistry platformQueryRegistry;

    @Test
    @DisplayName("Should generate correct YouTube query")
    void buildPlatformQuery_forYoutube_returnsCorrectQuery() {
        TargetMusicDto target = new TargetMusicDto("id", "Song Title", "Artist Name", "Album Name", null, "180000", null);
        String normalizedName = "song title";
        when(normalizer.normalizeForQuery("Song Title")).thenReturn(normalizedName);

        String expectedQuery = "song title music";
        String actualQuery = platformQueryRegistry.buildPlatformQuery(target, StreamingPlatform.YOUTUBE);

        assertEquals(expectedQuery, actualQuery);
        verify(normalizer, times(1)).normalizeForQuery("Song Title");
    }

    @Test
    @DisplayName("Should generate correct SoundCloud query")
    void buildPlatformQuery_forSoundcloud_returnsCorrectQuery() {
        TargetMusicDto target = new TargetMusicDto("id", "Song Title", "Artist Name", "Album Name", null, "180000", null);
        String normalizedArtist = "artist name";
        String normalizedName = "song title";
        when(normalizer.normalizeForQuery("Artist Name")).thenReturn(normalizedArtist);
        when(normalizer.normalizeForQuery("Song Title")).thenReturn(normalizedName);

        String expectedQuery = "artist name song title";
        String actualQuery = platformQueryRegistry.buildPlatformQuery(target, StreamingPlatform.SOUNDCLOUD);

        assertEquals(expectedQuery, actualQuery);
        verify(normalizer, times(1)).normalizeForQuery("Artist Name");
        verify(normalizer, times(1)).normalizeForQuery("Song Title");
    }

    @Test
    @DisplayName("Should generate correct Spotify query")
    void buildPlatformQuery_forSpotify_returnsCorrectQuery() {
        TargetMusicDto target = new TargetMusicDto("id", "Song Title", "Artist Name", "Album Name", null, "180000", null);
        String normalizedName = "song title";
        when(normalizer.normalizeForQuery("Song Title")).thenReturn(normalizedName);

        String expectedQuery = "song title";
        String actualQuery = platformQueryRegistry.buildPlatformQuery(target, StreamingPlatform.SPOTIFY);

        assertEquals(expectedQuery, actualQuery);
        verify(normalizer, times(1)).normalizeForQuery("Song Title");
    }


}
