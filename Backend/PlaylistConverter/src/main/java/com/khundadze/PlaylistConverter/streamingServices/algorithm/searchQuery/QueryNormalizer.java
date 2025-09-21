package com.khundadze.PlaylistConverter.streamingServices.algorithm.searchQuery;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class QueryNormalizer {

    // Pre-compiled patterns for better performance
    private static final Pattern REMOVE_BRACKETS_CONTENT = Pattern.compile("\\(.*?\\)|\\[.*?]");
    private static final Pattern REMOVE_FEATURING = Pattern.compile("(?i)\\s+(feat|ft)\\.?\\s+.*");
    private static final Pattern REMOVE_COMMON_TERMS = Pattern.compile("(?i)(official music video|music video|official video|official audio|visualizer|lyric video|lyrics|hq|hd|remastered|\\d{4} remaster)");
    private static final Pattern NORMALIZE_WHITESPACE = Pattern.compile("\\s+");


    public String normalizeForQuery(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        String normalized = input.toLowerCase();

        normalized = REMOVE_COMMON_TERMS.matcher(normalized).replaceAll("");

        normalized = REMOVE_BRACKETS_CONTENT.matcher(normalized).replaceAll("");

        normalized = REMOVE_FEATURING.matcher(normalized).replaceAll("");

        normalized = NORMALIZE_WHITESPACE.matcher(normalized).replaceAll(" ").trim();

        return normalized;
    }
}
