package com.khundadze.PlaylistConverter.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.models_db.OAuthToken;
import com.khundadze.PlaylistConverter.models_db.OAuthTokenId;

public interface OAuthTokenRepository extends JpaRepository<OAuthToken, OAuthTokenId> {
    List<OAuthToken> findAllByUser_Id(Long userId);

    Optional<OAuthToken> findByIdUserIdAndIdPlatform(Long userId, StreamingPlatform platform);
}
