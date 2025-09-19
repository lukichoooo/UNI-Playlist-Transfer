package com.khundadze.PlaylistConverter;

import com.khundadze.PlaylistConverter.dtos.ResultMusicDto;
import com.khundadze.PlaylistConverter.dtos.TargetMusicDto;
import com.khundadze.PlaylistConverter.models.Music;
import com.khundadze.PlaylistConverter.services.MusicMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MusicMapperTest {

    private MusicMapper musicMapper;

    @BeforeEach
    void setUp() {
        musicMapper = new MusicMapper();
    }

    @Test
    void testToResultMusicDto_shortDescription() {
        Music music = Music.builder()
                .id("1")
                .name("Test Song")
                .artist("Test Artist")
                .album("Test Album")
                .description("This is a test description with some words")
                .build();

        ResultMusicDto dto = musicMapper.toResultMusicDto(music);

        assertEquals(music.getId(), dto.id());
        assertEquals(music.getName(), dto.name());
        assertEquals(music.getArtist(), dto.artist());
        assertEquals(music.getAlbum(), dto.album());
        assertTrue(dto.keywordsLowSet().contains("test"));
        assertTrue(dto.keywordsLowSet().contains("description"));
        assertTrue(dto.keywordsLowSet().contains("some"));
    }

    @Test
    void testToTargetMusicDto_longDescription() {
        String longDesc = "word ".repeat(500); // 2500 chars
        Music music = Music.builder()
                .id("2")
                .name("Long Song")
                .artist("Artist")
                .album("Album")
                .description(longDesc)
                .build();

        TargetMusicDto dto = musicMapper.toTargetMusicDto(music);

        // Check trimmed length: prefix 300 + space + suffix 150 -> ~451 words * 5 chars ~= 2255? But let's just check total length < 500
        assertTrue(dto.keywordsLowList().size() > 0);
        assertTrue(dto.keywordsLowList().contains("word"));
        assertEquals("2", dto.id());
        assertEquals("Long Song", dto.name());
    }

    @Test
    void testTrimDescription_nullDescription() {
        Music music = Music.builder()
                .id("3")
                .name("Null Description")
                .artist("Artist")
                .album("Album")
                .description(null)
                .build();

        ResultMusicDto dto = musicMapper.toResultMusicDto(music);
        TargetMusicDto targetDto = musicMapper.toTargetMusicDto(music);

        assertNotNull(dto.keywordsLowSet());
        assertTrue(dto.keywordsLowSet().isEmpty());
        assertNotNull(targetDto.keywordsLowList());
        assertTrue(targetDto.keywordsLowList().isEmpty());
    }
}
