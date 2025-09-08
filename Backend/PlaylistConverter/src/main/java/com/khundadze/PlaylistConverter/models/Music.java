package com.khundadze.PlaylistConverter.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Music { // TODO: maybe turn into a record (idk how this will be with redis)

    private String id;

    private String title;
    private String artist;
    private String album;
}
