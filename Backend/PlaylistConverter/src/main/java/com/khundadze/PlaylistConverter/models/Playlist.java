package com.khundadze.PlaylistConverter.models;

import java.util.ArrayList;
import java.util.List;

import com.khundadze.PlaylistConverter.enums.StreamingPlatform;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Playlist {

    private Long id; // id on the platform
    private Long userId; // owner
    private String name;

    private StreamingPlatform streamingPlatform; // e.g., "spotify", "youtube"

    @Builder.Default
    private List<Music> musics = new ArrayList<>();

    public void addMusic(Music music) {
        musics.add(music);
    }

    public void removeMusic(Music music) {
        musics.remove(music);
    }
}
