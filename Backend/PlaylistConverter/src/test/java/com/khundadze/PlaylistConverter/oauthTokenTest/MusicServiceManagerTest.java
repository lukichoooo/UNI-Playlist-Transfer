package com.khundadze.PlaylistConverter.oauthTokenTest;

import com.khundadze.PlaylistConverter.dtos.OAuthTokenResponseDto;
import com.khundadze.PlaylistConverter.dtos.PlaylistSearchDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.exceptions.UserNotAuthorizedForStreamingPlatformException;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.services.OAuthTokenService;
import com.khundadze.PlaylistConverter.streamingServices.MusicService;
import com.khundadze.PlaylistConverter.streamingServices.MusicServiceManager;
import com.khundadze.PlaylistConverter.streamingServices.StreamingPlatformRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MusicServiceManagerTest {

    @Mock
    private StreamingPlatformRegistry registry;

    @Mock
    private OAuthTokenService tokenService;

    @Mock
    private MusicService spotifyService;

    @InjectMocks
    private MusicServiceManager manager;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUsersPlaylists_shouldReturnSortedPlaylists() {
        StreamingPlatform platform = StreamingPlatform.SPOTIFY;
        OAuthTokenResponseDto tokenDto = new OAuthTokenResponseDto("token123", platform);

        PlaylistSearchDto p1 = new PlaylistSearchDto("id1", "b Playlist", 1);
        PlaylistSearchDto p2 = new PlaylistSearchDto("id2", "a Playlist", 2);

        when(tokenService.getValidAccessTokenDto(platform)).thenReturn(tokenDto);
        when(registry.getService(platform)).thenReturn(spotifyService);
        when(spotifyService.getUsersPlaylists("token123")).thenReturn(List.of(p1, p2));

        List<PlaylistSearchDto> result = manager.getUsersPlaylists(platform);

        assertEquals(2, result.size());
        assertEquals("a Playlist", result.get(0).name());
        assertEquals("b Playlist", result.get(1).name());

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

    @Test
    void transferPlaylist_shouldTransferTracks() {
        // TODO: unimplemented method
    }
}
