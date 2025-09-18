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
        return id.getPlatform();
    }

    public void setPlatform(StreamingPlatform service) {
        id.setPlatform(service);
    }

    public Long getUserId() {
        return id.getUserId();
    }

    public void setUserId(Long userId) {
        id.setUserId(userId);
    }

}