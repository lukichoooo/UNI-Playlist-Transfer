package com.khundadze.PlaylistConverter.securityConfig.oauth2ForPlatformAuth;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.services.CurrentUserProvider;
import com.khundadze.PlaylistConverter.services.OAuthTokenService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuthStreamingPlatformTokenHandler implements AuthenticationSuccessHandler {

    private final OAuthTokenService tokenService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final String frontendUrl = "http://localhost:5173";

    private final CurrentUserProvider userProvider;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            response.sendRedirect(frontendUrl + "/login");
            return;
        }

        // Get platform enum from registrationId
        StreamingPlatform platform = StreamingPlatform.fromString(oauthToken.getAuthorizedClientRegistrationId());

        // // Get user attributes
        // Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();
        // Long userId = extractUserId(attributes);

        // Fetch the OAuth2AuthorizedClient from the service
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName());

        if (client == null) {
            response.sendRedirect(frontendUrl + "/login?error=client_not_found");
            return;
        }

        OAuth2AccessToken accessToken = client.getAccessToken();
        OAuth2RefreshToken refreshToken = client.getRefreshToken();
        Instant expiry = accessToken != null ? accessToken.getExpiresAt() : null;

        if (userProvider.isLoggedIn()) {
            // Save token to DB
            tokenService.save(
                    platform,
                    accessToken != null ? accessToken.getTokenValue() : null,
                    refreshToken != null ? refreshToken.getTokenValue() : null,
                    expiry);
        }

        // Redirect to frontend
        response.sendRedirect(frontendUrl + "/platform-oauth-success?platform=" + platform);
    }

    // private Long extractUserId(Map<String, Object> attributes) {
    // if (attributes.containsKey("id")) {
    // Object idObj = attributes.get("id");
    // if (idObj instanceof Number number)
    // return number.longValue();
    // try {
    // return Long.parseLong(idObj.toString());
    // } catch (NumberFormatException ignored) {
    // }
    // }
    // return System.currentTimeMillis(); // fallback
    // }
}
