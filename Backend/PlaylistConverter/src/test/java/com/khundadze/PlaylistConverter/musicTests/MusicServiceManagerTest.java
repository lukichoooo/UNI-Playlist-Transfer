package com.khundadze.PlaylistConverter.musicTests;

import com.khundadze.PlaylistConverter.dtos.OAuthTokenResponseDto;
import com.khundadze.PlaylistConverter.dtos.PlaylistSearchDto;
import com.khundadze.PlaylistConverter.dtos.TargetMusicDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.exceptions.UserNotAuthorizedForStreamingPlatformException;
import com.khundadze.PlaylistConverter.models.Music;
import com.khundadze.PlaylistConverter.models.Playlist;
import com.khundadze.PlaylistConverter.services.OAuthTokenService;
import com.khundadze.PlaylistConverter.streamingServices.MusicService;
import com.khundadze.PlaylistConverter.streamingServices.MusicServiceManager;
import com.khundadze.PlaylistConverter.streamingServices.StreamingPlatformRegistry;
import com.khundadze.PlaylistConverter.streamingServices.progressBar.ProgressService;
import com.khundadze.PlaylistConverter.streamingServices.progressBar.TransferProgress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MusicServiceManagerTest {

    @Mock
    private StreamingPlatformRegistry registry;
    @Mock
    private OAuthTokenService tokenService;
    @Mock
    private ProgressService progressService;
    @Mock
    private MusicService musicService;

    @InjectMocks
    private MusicServiceManager musicServiceManager;

    private static final StreamingPlatform TEST_PLATFORM = StreamingPlatform.SPOTIFY;
    private static final String FAKE_TOKEN = "fake-access-token";
    private final OAuthTokenResponseDto tokenDto = new OAuthTokenResponseDto(FAKE_TOKEN, TEST_PLATFORM);

    @BeforeEach
    void setUp() {
        when(registry.getService(any(StreamingPlatform.class))).thenReturn(musicService);
    }

    @Test
    @DisplayName("getUsersPlaylists should return sorted playlists when token is valid")
    void getUsersPlaylists_Success() {
        PlaylistSearchDto playlistC = new PlaylistSearchDto("id3", "C Playlist", 30);
        PlaylistSearchDto playlistA = new PlaylistSearchDto("id1", "a playlist", 10);
        PlaylistSearchDto playlistB = new PlaylistSearchDto("id2", "B Playlist", 20);
        List<PlaylistSearchDto> unsortedPlaylists = Arrays.asList(playlistC, playlistA, playlistB);
        when(tokenService.getValidAccessTokenDto(TEST_PLATFORM)).thenReturn(tokenDto);
        when(musicService.getUsersPlaylists(FAKE_TOKEN)).thenReturn(unsortedPlaylists);

        List<PlaylistSearchDto> result = musicServiceManager.getUsersPlaylists(TEST_PLATFORM);

        assertEquals(3, result.size());
        assertEquals("a playlist", result.get(0).name());
        assertEquals("B Playlist", result.get(1).name());
        assertEquals("C Playlist", result.get(2).name());
        verify(tokenService).getValidAccessTokenDto(TEST_PLATFORM);
        verify(registry).getService(TEST_PLATFORM);
        verify(musicService).getUsersPlaylists(FAKE_TOKEN);
    }

    @Test
    @DisplayName("getUsersPlaylists should throw exception when no valid token is found")
    void getUsersPlaylists_NoToken_ThrowsException() {
        when(tokenService.getValidAccessTokenDto(TEST_PLATFORM)).thenReturn(null);
        var exception = assertThrows(UserNotAuthorizedForStreamingPlatformException.class, () -> musicServiceManager.getUsersPlaylists(TEST_PLATFORM));
        assertEquals("No valid token for " + TEST_PLATFORM, exception.getMessage());
        verify(musicService, never()).getUsersPlaylists(any());
    }

    @Test
    @DisplayName("createPlaylist should succeed when token is valid")
    void createPlaylist_Success() {
        String playlistName = "My New Playlist";
        List<String> trackIds = List.of("track1", "track2");
        Playlist expectedPlaylist = Playlist.builder().id("newId").name(playlistName).build();
        when(tokenService.getValidAccessTokenDto(TEST_PLATFORM)).thenReturn(tokenDto);
        when(musicService.createPlaylist(FAKE_TOKEN, playlistName, trackIds)).thenReturn(expectedPlaylist);

        Playlist result = musicServiceManager.createPlaylist(TEST_PLATFORM, playlistName, trackIds);

        assertEquals(expectedPlaylist, result);
        verify(tokenService).getValidAccessTokenDto(TEST_PLATFORM);
        verify(musicService).createPlaylist(FAKE_TOKEN, playlistName, trackIds);
    }

    @Test
    @DisplayName("createPlaylist should throw exception when no valid token is found")
    void createPlaylist_NoToken_ThrowsException() {
        when(tokenService.getValidAccessTokenDto(TEST_PLATFORM)).thenReturn(null);
        assertThrows(UserNotAuthorizedForStreamingPlatformException.class, () -> musicServiceManager.createPlaylist(TEST_PLATFORM, "name", Collections.emptyList()));
        verify(musicService, never()).createPlaylist(any(), any(), any());
    }

    @Test
    @DisplayName("transferPlaylist should successfully transfer all tracks with a given name")
    void transferPlaylist_Success() {
        // Arrange
        StreamingPlatform fromPlatform = StreamingPlatform.SPOTIFY;
        StreamingPlatform toPlatform = StreamingPlatform.YOUTUBEMUSIC;
        String newPlaylistName = "Transferred Playlist";
        String transferState = "transfer-123";

        MusicService fromService = mock(MusicService.class);
        MusicService toService = mock(MusicService.class);

        when(registry.getService(fromPlatform)).thenReturn(fromService);
        when(registry.getService(toPlatform)).thenReturn(toService);

        OAuthTokenResponseDto fromToken = new OAuthTokenResponseDto("from-token", fromPlatform);
        OAuthTokenResponseDto toToken = new OAuthTokenResponseDto("to-token", toPlatform);
        when(tokenService.getValidAccessTokenDto(fromPlatform)).thenReturn(fromToken);
        when(tokenService.getValidAccessTokenDto(toPlatform)).thenReturn(toToken);

        List<TargetMusicDto> sourceTracks = List.of(
                new TargetMusicDto("s_id1", "Track A", "Artist X", "Album A", "ISRC1", "180", List.of("pop")),
                new TargetMusicDto("s_id2", "Track B", "Artist Y", "Album B", "ISRC2", "240", List.of("rock"))
        );
        when(fromService.getPlaylistsTracks(eq(fromToken.accessToken()), eq("fromId"))).thenReturn(sourceTracks);

        when(toService.findTrackId(eq(toToken.accessToken()), any(TargetMusicDto.class), eq(fromPlatform)))
                .thenReturn("target-id-A")
                .thenReturn("target-id-B");

        List<String> expectedTargetIds = List.of("target-id-A", "target-id-B");
        Playlist expectedCreatedPlaylist = Playlist.builder()
                .id("new-id")
                .name(newPlaylistName)
                .musics(Stream.generate(() -> mock(Music.class)).limit(expectedTargetIds.size()).toList())
                .build();
        when(toService.createPlaylist(eq(toToken.accessToken()), eq(newPlaylistName), eq(expectedTargetIds))).thenReturn(expectedCreatedPlaylist);

        // Act
        Playlist result = musicServiceManager.transferPlaylist(transferState, fromPlatform, toPlatform, "fromId", newPlaylistName);

        // Assert
        assertEquals(expectedCreatedPlaylist, result);

        ArgumentCaptor<List<String>> trackIdsCaptor = ArgumentCaptor.forClass(List.class);
        verify(toService).createPlaylist(eq(toToken.accessToken()), eq(newPlaylistName), trackIdsCaptor.capture());
        assertEquals(expectedTargetIds, trackIdsCaptor.getValue());

        ArgumentCaptor<TransferProgress> progressCaptor = ArgumentCaptor.forClass(TransferProgress.class);
        verify(progressService, times(3)).sendProgress(eq(transferState), progressCaptor.capture());
        List<TransferProgress> progresses = progressCaptor.getAllValues();
        assertEquals(2, progresses.get(1).getCurrent());
        assertEquals(2, progresses.get(1).getTotal());
        assertEquals(expectedCreatedPlaylist.getMusicCount(), progresses.get(2).getCurrent());
        assertEquals(expectedCreatedPlaylist.getMusicCount(), progresses.get(2).getTotal());
    }

    @Test
    @DisplayName("transferPlaylist should use a default name if none is provided")
    void transferPlaylist_UsesDefaultName() {
        // Given
        StreamingPlatform fromPlatform = StreamingPlatform.SPOTIFY;
        String expectedDefaultName = "Playlist From " + fromPlatform.name();
        when(tokenService.getValidAccessTokenDto(any())).thenReturn(tokenDto);
        when(musicService.getPlaylistsTracks(any(), any())).thenReturn(Collections.emptyList());
        Playlist dummyPlaylist = Playlist.builder().id("dummy-id").name(expectedDefaultName).build();
        when(musicService.createPlaylist(anyString(), anyString(), anyList())).thenReturn(dummyPlaylist);

        // When
        musicServiceManager.transferPlaylist("state", fromPlatform, StreamingPlatform.YOUTUBEMUSIC, "id", "");

        // Then
        verify(musicService).createPlaylist(eq(FAKE_TOKEN), eq(expectedDefaultName), any());
    }

    @Test
    @DisplayName("transferPlaylist should filter out tracks that are not found")
    void transferPlaylist_FiltersNotFoundTracks() {
        // Arrange
        StreamingPlatform fromPlatform = StreamingPlatform.SPOTIFY;
        StreamingPlatform toPlatform = StreamingPlatform.YOUTUBEMUSIC;
        String transferState = "state";
        String newPlaylistName = "name";

        MusicService fromService = mock(MusicService.class);
        MusicService toService = mock(MusicService.class);
        when(registry.getService(fromPlatform)).thenReturn(fromService);
        when(registry.getService(toPlatform)).thenReturn(toService);

        OAuthTokenResponseDto fromToken = new OAuthTokenResponseDto("from-token", fromPlatform);
        OAuthTokenResponseDto toToken = new OAuthTokenResponseDto("to-token", toPlatform);
        when(tokenService.getValidAccessTokenDto(fromPlatform)).thenReturn(fromToken);
        when(tokenService.getValidAccessTokenDto(toPlatform)).thenReturn(toToken);

        List<TargetMusicDto> sourceTracks = List.of(
                new TargetMusicDto("s_id1", "Track A", "X", "A", "I1", "180", List.of()),
                new TargetMusicDto("s_id2", "Track B", "Y", "B", "I2", "240", List.of()),
                new TargetMusicDto("s_id3", "Track C", "Z", "C", "I3", "200", List.of())
        );
        when(fromService.getPlaylistsTracks(eq(fromToken.accessToken()), anyString())).thenReturn(sourceTracks);

        when(toService.findTrackId(eq(toToken.accessToken()), any(TargetMusicDto.class), eq(fromPlatform)))
                .thenReturn("id-A")
                .thenReturn(null)
                .thenReturn("id-C");

        List<String> expectedTrackIds = List.of("id-A", "id-C");
        Playlist dummyPlaylist = Playlist.builder().id("dummy-id").name(newPlaylistName).build();
        when(toService.createPlaylist(eq(toToken.accessToken()), eq(newPlaylistName), eq(expectedTrackIds))).thenReturn(dummyPlaylist);

        // Act
        musicServiceManager.transferPlaylist(transferState, fromPlatform, toPlatform, "id", newPlaylistName);

        // Assert
        ArgumentCaptor<List<String>> trackIdsCaptor = ArgumentCaptor.forClass(List.class);
        verify(toService).createPlaylist(eq(toToken.accessToken()), eq(newPlaylistName), trackIdsCaptor.capture());
        List<String> capturedIds = trackIdsCaptor.getValue();
        assertEquals(2, capturedIds.size());
        assertTrue(capturedIds.containsAll(expectedTrackIds));

        ArgumentCaptor<TransferProgress> progressCaptor = ArgumentCaptor.forClass(TransferProgress.class);
        verify(progressService, times(4)).sendProgress(eq(transferState), progressCaptor.capture());
        List<TransferProgress> progresses = progressCaptor.getAllValues();
        assertTrue(progresses.get(0).isFound());
        assertFalse(progresses.get(1).isFound());
        assertTrue(progresses.get(2).isFound());
    }

    @Test
    @DisplayName("transferPlaylist should throw exception if 'from' or 'to' token is missing")
    void transferPlaylist_NoToken_ThrowsException() {
        when(tokenService.getValidAccessTokenDto(StreamingPlatform.SPOTIFY)).thenReturn(null);
        assertThrows(
                UserNotAuthorizedForStreamingPlatformException.class,
                () -> musicServiceManager.transferPlaylist("s", StreamingPlatform.SPOTIFY, StreamingPlatform.YOUTUBEMUSIC, "id", "n")
        );
        when(tokenService.getValidAccessTokenDto(StreamingPlatform.SPOTIFY)).thenReturn(tokenDto);
        when(tokenService.getValidAccessTokenDto(StreamingPlatform.YOUTUBEMUSIC)).thenReturn(null);
        assertThrows(
                UserNotAuthorizedForStreamingPlatformException.class,
                () -> musicServiceManager.transferPlaylist("s", StreamingPlatform.SPOTIFY, StreamingPlatform.YOUTUBEMUSIC, "id", "n")
        );
        verify(musicService, never()).getPlaylistsTracks(any(), any());
    }

    @Test
    @DisplayName("transferPlaylist should successfully transfer tracks when fromPlatform and toPlatform are the same")
    void transferPlaylist_SamePlatform_Success() {
        // Given
        StreamingPlatform platform = StreamingPlatform.SPOTIFY;
        String transferState = "transfer-456";
        String newPlaylistName = "Cloned Playlist";
        String fromPlaylistId = "from-playlist-id";

        MusicService svc = mock(MusicService.class);
        when(registry.getService(platform)).thenReturn(svc);
        OAuthTokenResponseDto token = new OAuthTokenResponseDto("same-platform-token", platform);
        when(tokenService.getValidAccessTokenDto(platform)).thenReturn(token);

        List<TargetMusicDto> sourceTracks = List.of(
                new TargetMusicDto("track-id1", "Track 1", "Artist 1", "Album 1", null, "200", List.of()),
                new TargetMusicDto("track-id2", "Track 2", "Artist 2", "Album 2", null, "250", List.of())
        );
        when(svc.getPlaylistsTracks(anyString(), eq(fromPlaylistId))).thenReturn(sourceTracks);

        Playlist createdPlaylist = Playlist.builder()
                .id("new-playlist-id")
                .name(newPlaylistName)
                .musics(List.of(Music.builder().id("track-id1").build(), Music.builder().id("track-id2").build()))
                .build();
        when(svc.createPlaylist(eq(token.accessToken()), eq(newPlaylistName), anyList())).thenReturn(createdPlaylist);

        // When
        Playlist result = musicServiceManager.transferPlaylist(transferState, platform, platform, fromPlaylistId, newPlaylistName);

        // Then
        assertEquals(createdPlaylist, result);
        ArgumentCaptor<List<String>> trackIdsCaptor = ArgumentCaptor.forClass(List.class);
        verify(svc).createPlaylist(eq(token.accessToken()), eq(newPlaylistName), trackIdsCaptor.capture());
        assertEquals(List.of("track-id1", "track-id2"), trackIdsCaptor.getValue());
        verify(svc, never()).findTrackId(anyString(), any(), any());
        verify(progressService, times(3)).sendProgress(eq(transferState), any(TransferProgress.class));
    }

    @Test
    @DisplayName("getUsersPlaylists should return an empty list when no playlists are found")
    void getUsersPlaylists_EmptyList() {
        // Given
        when(tokenService.getValidAccessTokenDto(TEST_PLATFORM)).thenReturn(tokenDto);
        when(musicService.getUsersPlaylists(FAKE_TOKEN)).thenReturn(Collections.emptyList());

        // When
        List<PlaylistSearchDto> result = musicServiceManager.getUsersPlaylists(TEST_PLATFORM);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(musicService).getUsersPlaylists(FAKE_TOKEN);
    }

    @Test
    @DisplayName("createPlaylist should successfully create an empty playlist when given an empty track list")
    void createPlaylist_EmptyTrackList_Success() {
        // Given
        String playlistName = "Empty Playlist";
        List<String> trackIds = Collections.emptyList();
        Playlist expectedPlaylist = Playlist.builder().id("emptyId").name(playlistName).musics(Collections.emptyList()).build();
        when(tokenService.getValidAccessTokenDto(TEST_PLATFORM)).thenReturn(tokenDto);
        when(musicService.createPlaylist(FAKE_TOKEN, playlistName, trackIds)).thenReturn(expectedPlaylist);

        // When
        Playlist result = musicServiceManager.createPlaylist(TEST_PLATFORM, playlistName, trackIds);

        // Then
        assertEquals(expectedPlaylist, result);
        assertEquals(0, result.getMusicCount());
        verify(musicService).createPlaylist(FAKE_TOKEN, playlistName, trackIds);
    }
}