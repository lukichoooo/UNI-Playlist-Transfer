package com.khundadze.PlaylistConverter.models;

import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class Playlist {

    private String id; // id on the platform
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

    public int getMusicCount() {
        return musics.size();
    }
}
