package com.khundadze.PlaylistConverter.securityConfig.oauth2ForPlatformAuth;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.services.CurrentUserProvider;
import com.khundadze.PlaylistConverter.services.OAuthTokenService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/platformAuth")
@RequiredArgsConstructor
public class PlatformAuthController {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuthTokenService oauthTokenService;
    private final CurrentUserProvider currentUserProvider;

    // Temporary in-memory store for unauthenticated users
    private final Map<String, OAuth2AccessTokenResponse> tempTokens = new ConcurrentHashMap<>();

    @GetMapping("/connect/youtube")
    public void connectYoutube(HttpServletResponse response) throws IOException {
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("youtube");

        String state = UUID.randomUUID().toString(); // CSRF/state mapping

        String authUri = UriComponentsBuilder
                .fromUriString(registration.getProviderDetails().getAuthorizationUri())
                .queryParam("client_id", registration.getClientId())
                .queryParam("response_type", "code")
                .queryParam("scope", String.join(" ", registration.getScopes()))
                .queryParam("redirect_uri", registration.getRedirectUri().replace("{baseUrl}", "http://localhost:8080"))
                .queryParam("state", state)
                .queryParam("access_type", "offline")
                .build()
                .toUriString();

        response.sendRedirect(authUri);
    }

    @GetMapping("/callback/youtube")
    public void youtubeCallback(@RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            HttpServletResponse response) throws IOException {

        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("youtube");

        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri(registration.getProviderDetails().getAuthorizationUri())
                .clientId(registration.getClientId())
                .redirectUri(registration.getRedirectUri().replace("{baseUrl}", "http://localhost:8080"))
                .scopes(registration.getScopes())
                .state(state)
                .build();

        OAuth2AuthorizationResponse authorizationResponse = OAuth2AuthorizationResponse.success(code)
                .redirectUri(registration.getRedirectUri().replace("{baseUrl}", "http://localhost:8080"))
                .build();

        OAuth2AuthorizationCodeGrantRequest grantRequest = new OAuth2AuthorizationCodeGrantRequest(
                registration,
                new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse));

        OAuth2AccessTokenResponse tokenResponse = new DefaultAuthorizationCodeTokenResponseClient()
                .getTokenResponse(grantRequest);

        String tokenValue;
        if (currentUserProvider.isLoggedIn()) {
            // Save in DB
            oauthTokenService.save(
                    StreamingPlatform.YOUTUBE,
                    tokenResponse.getAccessToken().getTokenValue(),
                    tokenResponse.getRefreshToken() != null ? tokenResponse.getRefreshToken().getTokenValue() : null,
                    tokenResponse.getAccessToken().getExpiresAt());
            tokenValue = tokenResponse.getAccessToken().getTokenValue();
        } else {
            // Save temporarily in memory
            tempTokens.put(state != null ? state : UUID.randomUUID().toString(), tokenResponse);
            tokenValue = tokenResponse.getAccessToken().getTokenValue();
        }
        response.sendRedirect("http://localhost:5173/platform-auth-success");
    }

    @GetMapping("/tempToken")
    public OAuth2AccessTokenResponse getTempToken(@RequestParam String state) {
        return tempTokens.get(state);
    }
}
