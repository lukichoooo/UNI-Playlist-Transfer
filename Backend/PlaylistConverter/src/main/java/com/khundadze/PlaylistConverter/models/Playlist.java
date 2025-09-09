package com.khundadze.PlaylistConverter.models;

import java.util.ArrayList;
import java.util.List;

import com.khundadze.PlaylistConverter.enums.StreamingPlatform;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Playlist {

    private String id;
    private Long userId;

    private StreamingPlatform streamingPlatform; // e.g., "spotify", "youtube"

    @Builder.Default
    private List<Music> musics = new ArrayList<>();
    private String name;
}
