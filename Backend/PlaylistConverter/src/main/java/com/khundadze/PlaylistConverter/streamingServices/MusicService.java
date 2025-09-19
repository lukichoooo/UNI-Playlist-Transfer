package com.khundadze.PlaylistConverter.streamingServices;

import com.khundadze.PlaylistConverter.dtos.PlaylistSearchDto;
import com.khundadze.PlaylistConverter.dtos.ResultMusicDto;
import com.khundadze.PlaylistConverter.dtos.TargetMusicDto;
import com.khundadze.PlaylistConverter.models.Music;
import com.khundadze.PlaylistConverter.models.Playlist;

import java.util.List;

public abstract class MusicService {
    public abstract List<PlaylistSearchDto> getUsersPlaylists(String accessToken);

    public abstract List<TargetMusicDto> getPlaylistsTracks(String accessToken, String playlistId);
    // build the music object
    // MAX_DESCRIPTION_LENGTH = starting with 300 + ending with 150
    // store description as list of words

    public abstract Playlist createPlaylist(String accessToken, String playlistName, List<String> trackIds);

    public abstract String findTrackId(String accessToken, TargetMusicDto target);
    // if any of musics fields are null, search for them in description
    // store every word of that description in musics hashset "descriptionKeywords" (lowercase words)
    // now compare every word in targetsDescription with every word in keywords


    // TODO: improve song matching algorithm
    protected Music bestMatch(TargetMusicDto target, List<ResultMusicDto> results) {
        if (results.isEmpty()) return null; // handle empty list

        ResultMusicDto best = results.get(0);
        int bestScore = 0;

        for (ResultMusicDto result : results) {
            int score = 0;
            for (String keyword : target.keywordsLowList()) {
                if (result.keywordsLowSet().contains(keyword)) score++;
            }

            if (score > bestScore) {
                bestScore = score;
                best = result;
            }
        }

        return Music.builder()
                .id(best.id())
                .name(best.name())
                .artist(best.artist())
                .album(best.album())
                .description(null)
                .build();
    }

}
