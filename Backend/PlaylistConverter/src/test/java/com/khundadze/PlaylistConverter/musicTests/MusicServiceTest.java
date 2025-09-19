package com.khundadze.PlaylistConverter.musicTests;

import com.khundadze.PlaylistConverter.dtos.PlaylistSearchDto;
import com.khundadze.PlaylistConverter.dtos.ResultMusicDto;
import com.khundadze.PlaylistConverter.dtos.TargetMusicDto;
import com.khundadze.PlaylistConverter.models.Music;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.streamingServices.MusicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class MusicServiceTest {

    private TestMusicService musicService;

    @BeforeEach
    void setUp() {
        musicService = new TestMusicService();
    }

    @Test
    void testBestMatch_found() {
        TargetMusicDto target = new TargetMusicDto(
                "t1",
                "Target Song",
                "Target Artist",
                "Target Album",
                List.of("love", "music", "song")
        );

        ResultMusicDto r1 = new ResultMusicDto("r1", "Song 1", "Artist 1", "Album 1", new HashSet<>(Set.of("love", "song")));
        ResultMusicDto r2 = new ResultMusicDto("r2", "Song 2", "Artist 2", "Album 2", new HashSet<>(Set.of("music", "song", "love")));
        ResultMusicDto r3 = new ResultMusicDto("r3", "Song 3", "Artist 3", "Album 3", new HashSet<>(Set.of("other")));

        Music best = musicService.bestMatchPublic(target, List.of(r1, r2, r3));

        assertNotNull(best);
        assertEquals("r2", best.getId());
        assertEquals("Song 2", best.getName());
        assertEquals("Artist 2", best.getArtist());
    }

    @Test
    void testBestMatch_noMatch() {
        TargetMusicDto target = new TargetMusicDto(
                "t2",
                "Target Song",
                "Target Artist",
                "Target Album",
                List.of("keyword1", "keyword2")
        );

        ResultMusicDto r1 = new ResultMusicDto("r1", "Song 1", "Artist 1", "Album 1", new HashSet<>(Set.of("other1", "other2")));
        ResultMusicDto r2 = new ResultMusicDto("r2", "Song 2", "Artist 2", "Album 2", new HashSet<>(Set.of("other3")));

        Music best = musicService.bestMatchPublic(target, List.of(r1, r2));

        assertNotNull(best);
        assertEquals("r1", best.getId()); // first element should be returned
        assertEquals("Song 1", best.getName());
        assertEquals("Artist 1", best.getArtist());
    }

}


class TestMusicService extends MusicService {
    public Music bestMatchPublic(TargetMusicDto target, List<ResultMusicDto> results) {
        return super.bestMatch(target, results);
    }

    @Override
    public List<PlaylistSearchDto> getUsersPlaylists(String accessToken) {
        return List.of();
    }

    @Override
    public List<TargetMusicDto> getPlaylistsTracks(String accessToken, String playlistId) {
        return List.of();
    }

    @Override
    public Playlist createPlaylist(String accessToken, String playlistName, List<String> trackIds) {
        return null;
    }

    @Override
    public String findTrackId(String accessToken, TargetMusicDto target) {
        return "";
    }
}