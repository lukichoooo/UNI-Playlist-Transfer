package com.khundadze.PlaylistConverter.authenticationMVC;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse(
        @JsonProperty("accessToken") String token) {
}
