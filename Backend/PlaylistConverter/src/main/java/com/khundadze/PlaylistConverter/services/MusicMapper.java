package com.khundadze.PlaylistConverter.services;

import com.khundadze.PlaylistConverter.dtos.ResultMusicDto;
import com.khundadze.PlaylistConverter.dtos.TargetMusicDto;
import com.khundadze.PlaylistConverter.models.Music;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MusicMapper {

    public ResultMusicDto toResultMusicDto(Music music) {
        String trimmedDescription = trimDescription(music.getDescription());

        HashSet<String> keywords = Arrays.stream(trimmedDescription.split("\\s+"))
                .filter(w -> !w.isBlank() && w.length() > 2)
                .map(String::toLowerCase)
                .collect(Collectors.toCollection(HashSet::new));

        return new ResultMusicDto(
                music.getId(),
                music.getName(),
                music.getArtist(),
                music.getAlbum(),
                keywords
        );
    }

    public TargetMusicDto toTargetMusicDto(Music music) {
        String trimmedDescription = trimDescription(music.getDescription());

        List<String> keywords = Arrays.stream(trimmedDescription.split("\\s+"))
                .filter(w -> !w.isBlank() && w.length() > 2)
                .map(String::toLowerCase)
                .toList();

        return new TargetMusicDto(
                music.getId(),
                music.getName(),
                music.getArtist(),
                music.getAlbum(),
                keywords
        );
    }

    private String trimDescription(String description) {
        if (description == null) return "";
        if (description.length() <= 450) return description;

        String prefix = description.substring(0, 300);
        String suffix = description.substring(description.length() - 150);
        return prefix + " " + suffix;
    }
}
