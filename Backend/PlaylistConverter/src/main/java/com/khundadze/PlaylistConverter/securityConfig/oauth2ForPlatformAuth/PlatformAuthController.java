package com.khundadze.PlaylistConverter.securityConfig.oauth2ForPlatformAuth;

import com.khundadze.PlaylistConverter.dtos.OAuthTokenResponseDto;
import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.models_db.User;
import com.khundadze.PlaylistConverter.securityConfig.JwtService;
import com.khundadze.PlaylistConverter.services.OAuthTokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/platformAuth")
@RequiredArgsConstructor
public class PlatformAuthController {

    // Dependencies
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuthTokenService oauthTokenService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final StateManager stateManager;
    private final RestTemplate restTemplate = new RestTemplate();
    private final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> tokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient();

    @Value("${BASE_URL}")
    private String BASE_URL;

    @Value("${FRONTEND_URL}")
    private String FRONTEND_URL;

    /**
     * Initiates the OAuth2 connection flow for a given platform.
     * It handles both authenticated (with JWT) and anonymous users.
     */
    @GetMapping("/connect/{platform}")
    public void connectToPlatform(@PathVariable String platform,
                                  @RequestParam(value = "jwt_token", required = false) String token,
                                  HttpServletResponse response) throws IOException {

        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(platform);
        if (registration == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown platform: " + platform);
            return;
        }

        String state = stateManager.generateState();
        handleUserAuthentication(token, state);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(registration.getProviderDetails().getAuthorizationUri())
                .queryParam("client_id", registration.getClientId())
                .queryParam("response_type", "code")
                .queryParam("scope", String.join(" ", registration.getScopes()))
                .queryParam("redirect_uri", registration.getRedirectUri().replace("${BASE_URL}", BASE_URL))
                .queryParam("state", state);

        // Add platform-specific parameters
        switch (platform.toLowerCase()) {
            case "youtube", "youtubemusic":
                uriBuilder.queryParam("access_type", "offline");
                break;
            case "soundcloud":
                String codeVerifier = PKCEUtil.generateCodeVerifier();
                uriBuilder.queryParam("code_challenge", PKCEUtil.generateCodeChallenge(codeVerifier));
                uriBuilder.queryParam("code_challenge_method", "S256");
                stateManager.putCodeVerifier(state, codeVerifier);
                break;
            case "spotify":
                uriBuilder.queryParam("show_dialog", "true");
                break;
        }

        response.sendRedirect(uriBuilder.build().toUriString());
    }

    /**
     * Handles the OAuth2 callback from the platform provider.
     * It exchanges the authorization code for an access token.
     */
    @GetMapping("/callback/{platform}")
    public void platformCallback(@PathVariable String platform,
                                 @RequestParam("code") String code,
                                 @RequestParam("state") String state,
                                 HttpServletResponse response) throws IOException {

        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(platform);
        if (registration == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown platform: " + platform);
            return;
        }

        OAuth2AccessTokenResponse tokenResponse;
        if ("soundcloud".equalsIgnoreCase(platform)) {
            tokenResponse = getSoundCloudTokenResponse(code, state, registration);
        } else {
            tokenResponse = getStandardTokenResponse(code, state, registration);
        }

        processTokenResponse(state, tokenResponse, StreamingPlatform.valueOf(platform.toUpperCase()));

        response.sendRedirect(FRONTEND_URL + "/platform-auth-success");
    }

    /**
     * Retrieves a temporary token for an anonymous user flow.
     */
    @GetMapping("/tempToken")
    public OAuthTokenResponseDto getTempToken(@RequestParam String state, @RequestParam String platform) {
        OAuth2AccessTokenResponse tokenResponse = stateManager.removeTempToken(state);
        return new OAuthTokenResponseDto(tokenResponse.getAccessToken().getTokenValue(), StreamingPlatform.valueOf(platform.toUpperCase()));
    }

    // ===================================================================================
    // Private Helper Methods
    // ===================================================================================

    /**
     * If a JWT is provided, validates it and associates the user ID with the state.
     */
    private void handleUserAuthentication(String token, String state) {
        if (token != null && !token.isBlank()) {
            try {
                String username = jwtService.extractUsername(token);
                User user = (User) userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(token, user)) {
                    stateManager.putUser(state, user.getId());
                }
            } catch (Exception ignored) {
                // Ignore exceptions for invalid/expired tokens; proceed as an anonymous user.
            }
        }
    }

    /**
     * Exchanges the authorization code for a token using Spring's default client.
     * Used for standard OAuth2 providers like Google and Spotify.
     */
    private OAuth2AccessTokenResponse getStandardTokenResponse(String code, String state, ClientRegistration registration) {
        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri(registration.getProviderDetails().getAuthorizationUri())
                .clientId(registration.getClientId())
                .redirectUri(registration.getRedirectUri().replace("${BASE_URL}", BASE_URL))
                .scopes(registration.getScopes())
                .state(state)
                .build();

        OAuth2AuthorizationResponse authorizationResponse = OAuth2AuthorizationResponse.success(code)
                .redirectUri(registration.getRedirectUri().replace("${BASE_URL}", BASE_URL))
                .state(state)
                .build();

        OAuth2AuthorizationCodeGrantRequest grantRequest = new OAuth2AuthorizationCodeGrantRequest(
                registration,
                new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse));

        return tokenResponseClient.getTokenResponse(grantRequest);
    }

    /**
     * Exchanges the authorization code for a token manually for SoundCloud (using PKCE).
     */
    private OAuth2AccessTokenResponse getSoundCloudTokenResponse(String code, String state, ClientRegistration registration) {
        String codeVerifier = stateManager.removeCodeVerifier(state);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", registration.getClientId());
        form.add("client_secret", registration.getClientSecret());
        form.add("redirect_uri", registration.getRedirectUri().replace("${BASE_URL}", BASE_URL));
        form.add("code_verifier", codeVerifier);
        form.add("code", code);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        Map<String, Object> responseMap = restTemplate.postForObject(
                registration.getProviderDetails().getTokenUri(),
                new HttpEntity<>(form, headers),
                Map.class);

        Objects.requireNonNull(responseMap, "SoundCloud token response was null");

        return OAuth2AccessTokenResponse.withToken((String) responseMap.get("access_token"))
                .tokenType(OAuth2AccessToken.TokenType.BEARER)
                .refreshToken((String) responseMap.get("refresh_token"))
                .expiresIn(((Number) responseMap.get("expires_in")).longValue())
                .build();
    }

    /**
     * Processes the obtained token response.
     * If the user was authenticated, it saves the token to the database.
     * Otherwise, it stores the token temporarily for the anonymous flow.
     */
    private void processTokenResponse(String state, OAuth2AccessTokenResponse tokenResponse, StreamingPlatform platform) {
        Long userId = stateManager.removeUser(state);

        if (userId != null) {
            oauthTokenService.save(
                    userId,
                    platform,
                    tokenResponse.getAccessToken().getTokenValue(),
                    tokenResponse.getRefreshToken() != null ? tokenResponse.getRefreshToken().getTokenValue() : null,
                    tokenResponse.getAccessToken().getExpiresAt()
            );
        } else {
            stateManager.putTempToken(state, tokenResponse);
        }
    }
}