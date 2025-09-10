package com.khundadze.PlaylistConverter.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Music {

    private String id;

    private String name;
    private String artist;
    private String album;
}
