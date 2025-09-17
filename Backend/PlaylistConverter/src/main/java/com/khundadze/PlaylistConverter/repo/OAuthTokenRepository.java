package com.khundadze.PlaylistConverter.repo;

import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.models_db.OAuthToken;
import com.khundadze.PlaylistConverter.models_db.OAuthTokenId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OAuthTokenRepository extends JpaRepository<OAuthToken, OAuthTokenId> { // TODO: clear useless old tokens daily
    List<OAuthToken> findAllByUser_Id(Long userId);

    Optional<OAuthToken> findByIdUserIdAndIdPlatform(Long userId, StreamingPlatform platform);
}
