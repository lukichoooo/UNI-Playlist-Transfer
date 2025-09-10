package com.khundadze.PlaylistConverter.oauthTokenTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.khundadze.PlaylistConverter.dtos.OAuthTokenResponseDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.exceptions.UserNotAuthorizedForStreamingPlatformException;
import com.khundadze.PlaylistConverter.models.Music;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.services.OAuthTokenService;
import com.khundadze.PlaylistConverter.streamingServices.IMusicService;
import com.khundadze.PlaylistConverter.streamingServices.MusicServiceManager;
import com.khundadze.PlaylistConverter.streamingServices.StreamingPlatformRegistry;

class MusicServiceManagerTest {

    @Mock
    private StreamingPlatformRegistry registry;

    @Mock
    private OAuthTokenService tokenService;

    @Mock
    private IMusicService spotifyService;

    @InjectMocks
    private MusicServiceManager manager;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUsersPlaylists_shouldReturnPlaylists() {
        StreamingPlatform platform = StreamingPlatform.SPOTIFY;
        OAuthTokenResponseDto tokenDto = new OAuthTokenResponseDto("token123", platform);
        Playlist playlist = Playlist.builder().name("My Playlist").build();

        when(tokenService.getValidAccessTokenDto(platform)).thenReturn(tokenDto);
        when(registry.getService(platform)).thenReturn(spotifyService);
        when(spotifyService.getUsersPlaylists("token123")).thenReturn(List.of(playlist));

        List<Playlist> result = manager.getUsersPlaylists(platform);

        assertEquals(1, result.size());
        assertEquals("My Playlist", result.get(0).getName());
        verify(tokenService).getValidAccessTokenDto(platform);
        verify(spotifyService).getUsersPlaylists("token123");
    }

    @Test
    void getUsersPlaylists_noToken_shouldThrow() {
        StreamingPlatform platform = StreamingPlatform.SPOTIFY;
        when(tokenService.getValidAccessTokenDto(platform)).thenReturn(null);

        assertThrows(UserNotAuthorizedForStreamingPlatformException.class,
                () -> manager.getUsersPlaylists(platform));
    }

    @Test
    void getPlaylistTracks_shouldReturnTracks() {
        StreamingPlatform platform = StreamingPlatform.SPOTIFY;
        OAuthTokenResponseDto tokenDto = new OAuthTokenResponseDto("token123", platform);
        Music track = Music.builder().name("Song 1").build();

        when(tokenService.getValidAccessTokenDto(platform)).thenReturn(tokenDto);
        when(registry.getService(platform)).thenReturn(spotifyService);
        when(spotifyService.getPlaylistsTracks("token123", 42L)).thenReturn(List.of(track));

        List<Music> result = manager.getPlaylistTracks(platform, 42L);

        assertEquals(1, result.size());
        assertEquals("Song 1", result.get(0).getName());
        verify(tokenService).getValidAccessTokenDto(platform);
        verify(spotifyService).getPlaylistsTracks("token123", 42L);
    }

    @Test
    void createPlaylist_shouldReturnPlaylist() {
        StreamingPlatform platform = StreamingPlatform.SPOTIFY;
        OAuthTokenResponseDto tokenDto = new OAuthTokenResponseDto("token123", platform);
        Playlist playlist = Playlist.builder().name("New Playlist").build();

        when(tokenService.getValidAccessTokenDto(platform)).thenReturn(tokenDto);
        when(registry.getService(platform)).thenReturn(spotifyService);
        when(spotifyService.createPlaylist("token123", "New Playlist", List.of("track1", "track2")))
                .thenReturn(playlist);

        Playlist result = manager.createPlaylist(platform, "New Playlist", List.of("track1", "track2"));

        assertEquals("New Playlist", result.getName());
        verify(tokenService).getValidAccessTokenDto(platform);
        verify(spotifyService).createPlaylist("token123", "New Playlist", List.of("track1", "track2"));
    }
}
