package com.khundadze.PlaylistConverter.securityConfig.oauth2ForPlatformAuth;

import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.models_db.User;
import com.khundadze.PlaylistConverter.securityConfig.JwtService;
import com.khundadze.PlaylistConverter.services.OAuthTokenService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/platformAuth")
@RequiredArgsConstructor
public class PlatformAuthController {

    private final OAuthTokenService oauthTokenService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // Inject YouTube properties directly into this controller
    @Value("${spring.security.oauth2.client.registration.youtube.client-id}")
    private String youtubeClientId;
    @Value("${spring.security.oauth2.client.registration.youtube.client-secret}")
    private String youtubeClientSecret;

    private final Map<String, Long> stateToUserId = new ConcurrentHashMap<>();
    private final Map<String, OAuth2AccessTokenResponse> tempTokens = new ConcurrentHashMap<>();

    // Helper method to build the YouTube client registration on-the-fly
    private ClientRegistration getYoutubeClientRegistration() {
        return ClientRegistration.withRegistrationId("youtube")
                .clientId(youtubeClientId)
                .clientSecret(youtubeClientSecret)
                .scope("https://www.googleapis.com/auth/youtube.readonly", "https://www.googleapis.com/auth/youtube")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/api/platformAuth/callback/youtube")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .build();
    }

    @GetMapping("/connect/youtube")
    public void connectYoutube(
            @RequestParam(value = "jwt_token", required = false) String token,
            HttpServletResponse response) throws IOException {

        ClientRegistration registration = getYoutubeClientRegistration();
        String state = UUID.randomUUID().toString();

        if (token != null && !token.isBlank()) {
            try {
                String username = jwtService.extractUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(token, userDetails) && userDetails instanceof User) {
                    User user = (User) userDetails;
                    stateToUserId.put(state, user.getId());
                }
            } catch (Exception e) {
                // Ignore invalid tokens and proceed as an anonymous user
            }
        }

        String authUri = UriComponentsBuilder
                .fromUriString(registration.getProviderDetails().getAuthorizationUri())
                .queryParam("client_id", registration.getClientId())
                .queryParam("response_type", "code")
                .queryParam("scope", String.join(" ", registration.getScopes()))
                .queryParam("redirect_uri", registration.getRedirectUri())
                .queryParam("state", state)
                .queryParam("access_type", "offline")
                .build()
                .toUriString();

        response.sendRedirect(authUri);
    }

    @GetMapping("/callback/youtube")
    public void youtubeCallback(@RequestParam("code") String code,
                                @RequestParam("state") String state,
                                HttpServletResponse response) throws IOException {

        ClientRegistration registration = getYoutubeClientRegistration();

        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri(registration.getProviderDetails().getAuthorizationUri())
                .clientId(registration.getClientId())
                .redirectUri(registration.getRedirectUri())
                .scopes(registration.getScopes())
                .state(state)
                .build();

        OAuth2AuthorizationResponse authorizationResponse = OAuth2AuthorizationResponse.success(code)
                .redirectUri(registration.getRedirectUri())
                .build();

        OAuth2AuthorizationCodeGrantRequest grantRequest = new OAuth2AuthorizationCodeGrantRequest(
                registration,
                new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse));

        OAuth2AccessTokenResponse tokenResponse = new DefaultAuthorizationCodeTokenResponseClient()
                .getTokenResponse(grantRequest);

        Long userId = stateToUserId.remove(state);

        if (userId != null) {
            oauthTokenService.save(
                    StreamingPlatform.YOUTUBE,
                    tokenResponse.getAccessToken().getTokenValue(),
                    tokenResponse.getRefreshToken() != null ? tokenResponse.getRefreshToken().getTokenValue() : null,
                    tokenResponse.getAccessToken().getExpiresAt());
        } else {
            tempTokens.put(state, tokenResponse);
        }

        response.sendRedirect("http://localhost:5173/platform-auth-success");
    }

    @GetMapping("/tempToken")
    public OAuth2AccessTokenResponse getTempToken(@RequestParam String state) {
        return tempTokens.remove(state);
    }
}

