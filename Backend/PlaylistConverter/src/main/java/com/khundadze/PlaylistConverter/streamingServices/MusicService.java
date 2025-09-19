package com.khundadze.PlaylistConverter.streamingServices;

import com.khundadze.PlaylistConverter.dtos.PlaylistSearchDto;
import com.khundadze.PlaylistConverter.models.Music;
import com.khundadze.PlaylistConverter.models.Playlist;

import java.util.List;

public abstract class IMusicService {
    abstract List<PlaylistSearchDto> getUsersPlaylists(String accessToken);

    abstract List<Music> getPlaylistsTracks(String accessToken, String playlistId);
    // build the music object
    // MAX_DESCRIPTION_LENGTH = starting with 300 + ending with 150
    // store every word of that description in musics hashset "descriptionKeywords" (lowercase words)

    abstract Playlist createPlaylist(String accessToken, String playlistName, List<String> trackIds);

    abstract String findTrackId(String accessToken, Music music);
    // if any of musics fields are null, search for them in description
}
