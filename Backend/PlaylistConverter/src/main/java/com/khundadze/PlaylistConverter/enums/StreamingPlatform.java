package com.khundadze.PlaylistConverter.enums;

public enum StreamingPlatform {
    SPOTIFY, YOUTUBE, YOUTUBEMUSIC, SOUNDCLOUD, DEEZER, APPLEMUSIC;

    public static StreamingPlatform valueOfSafe(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown streaming provider: " + name);
        }
    }

    public static StreamingPlatform fromString(String registrationId) {
        if (registrationId == null || registrationId.isBlank()) {
            throw new IllegalArgumentException("registrationId cannot be null or empty");
        }
        return switch (registrationId.toLowerCase()) {
            case "spotify" -> SPOTIFY;
            case "youtube" -> YOUTUBE;
            case "youtubemusic" -> YOUTUBEMUSIC;
            case "soundcloud" -> SOUNDCLOUD;
            case "deezer" -> DEEZER;
            case "applemusic", "apple-music" -> APPLEMUSIC;
            default -> throw new IllegalArgumentException("Unknown streaming provider: " + registrationId);
        };
    }

    public static StreamingPlatform fromStringSafe(String registrationId) {
        try {
            return fromString(registrationId);
        } catch (IllegalArgumentException e) {
            return null; // not a streaming platform
        }
    }

}
