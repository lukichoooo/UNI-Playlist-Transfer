package com.khundadze.PlaylistConverter.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Music {

    private String id;
    private String name;
    private String artist;
    private String album;
    private String isrc;
    private String duration;

    private String description; // recieved from platform
}
