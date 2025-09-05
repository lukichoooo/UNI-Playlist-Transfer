package com.khundadze.PlaylistConverter.models_db;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.khundadze.enums.MusicService;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "oauth_tokens")
public class OAuthToken {

    @EmbeddedId
    private OAuthTokenId id;

    private String accessToken;
    private String refreshToken;
    private Instant expiresAt;

    @ManyToOne
    @MapsId("userId") // maps the userId part of the composite key
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    // getters and setters (for id parts)

    public MusicService getService() {
        return id.getService();
    }

    public Long getUserId() {
        return id.getUserId();
    }
}