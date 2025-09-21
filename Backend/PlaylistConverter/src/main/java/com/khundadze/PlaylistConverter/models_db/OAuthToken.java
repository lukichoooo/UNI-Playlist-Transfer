package com.khundadze.PlaylistConverter.models_db;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "oauth_token")
public class OAuthToken {

    @EmbeddedId
    private OAuthTokenId id;
    @Column(columnDefinition = "TEXT")
    private String accessToken;
    @Column(columnDefinition = "TEXT")
    private String refreshToken;
    private Instant expiresAt;

    @ManyToOne
    @MapsId("userId") // maps the userId part of the composite key
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    // getters and setters (for id parts)

    public StreamingPlatform getPlatform() {
        // Return null if id is not initialized
        return (this.id != null) ? this.id.getPlatform() : null;
    }

    public void setPlatform(StreamingPlatform service) {
        // Initialize id if it's null before using it
        if (this.id == null) {
            this.id = new OAuthTokenId();
        }
        this.id.setPlatform(service);
    }

    public Long getUserId() {
        return (this.id != null) ? this.id.getUserId() : null;
    }

    public void setUserId(Long userId) {
        if (this.id == null) {
            this.id = new OAuthTokenId();
        }
        this.id.setUserId(userId);
    }

}