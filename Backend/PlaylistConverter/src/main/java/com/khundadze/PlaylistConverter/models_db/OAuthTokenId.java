package com.khundadze.PlaylistConverter.models_db;

import java.io.Serializable;

import com.khundadze.PlaylistConverter.enums.StreamingPlatform;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@NoArgsConstructor
@Getter
@Setter
@Data
@AllArgsConstructor
@EqualsAndHashCode
public class OAuthTokenId implements Serializable {

    private Long userId;

    @Enumerated(EnumType.STRING)
    private StreamingPlatform platform;
}
