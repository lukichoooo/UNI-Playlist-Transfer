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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/platformAuth")
@RequiredArgsConstructor
public class PlatformAuthController {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuthTokenService oauthTokenService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final StateManager stateManager;

    @Value("${BASE_URL}")
    private String BASE_URL;

    @Value("${FRONTEND_URL}")
    private String FRONTEND_URL;


    @GetMapping("/tempToken")
    public OAuthTokenResponseDto getTempToken(@RequestParam String state, @RequestParam String platform) {
        OAuth2AccessTokenResponse tokenResponse = stateManager.removeTempToken(state);
        return new OAuthTokenResponseDto(tokenResponse.getAccessToken().getTokenValue(), StreamingPlatform.valueOf(platform));
    }


    @GetMapping("/connect/youtube")
    public void connectYoutube(
            @RequestParam(value = "jwt_token", required = false) String token,
            HttpServletResponse response) throws IOException {

        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("youtube");
        String state = stateManager.generateState();

        if (token != null && !token.isBlank()) {
            try {
                String username = jwtService.extractUsername(token);
                User user = (User) userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(token, user)) {
                    stateManager.putUser(state, user.getId());
                }
            } catch (Exception ignored) {
            }
        }

        String authUri = UriComponentsBuilder
                .fromUriString(registration.getProviderDetails().getAuthorizationUri())
                .queryParam("client_id", registration.getClientId())
                .queryParam("response_type", "code")
                .queryParam("scope", String.join(" ", registration.getScopes()))
                .queryParam("redirect_uri", registration.getRedirectUri().replace("${BASE_URL}", BASE_URL))
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

        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("youtube");

        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri(registration.getProviderDetails().getAuthorizationUri())
                .clientId(registration.getClientId())
                .redirectUri(registration.getRedirectUri().replace("${BASE_URL}", BASE_URL))
                .scopes(registration.getScopes())
                .state(state)
                .build();

        OAuth2AuthorizationResponse authorizationResponse = OAuth2AuthorizationResponse.success(code)
                .redirectUri(registration.getRedirectUri().replace("${BASE_URL}", BASE_URL))
                .build();

        OAuth2AuthorizationCodeGrantRequest grantRequest = new OAuth2AuthorizationCodeGrantRequest(
                registration,
                new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse));

        OAuth2AccessTokenResponse tokenResponse = new DefaultAuthorizationCodeTokenResponseClient()
                .getTokenResponse(grantRequest);

        Long userId = stateManager.removeUser(state);

        if (userId != null) {
            oauthTokenService.save(
                    userId,
                    StreamingPlatform.YOUTUBE,
                    tokenResponse.getAccessToken().getTokenValue(),
                    tokenResponse.getRefreshToken() != null ? tokenResponse.getRefreshToken().getTokenValue() : null,
                    tokenResponse.getAccessToken().getExpiresAt());
        } else {
            stateManager.putTempToken(state, tokenResponse);
        }

        response.sendRedirect(FRONTEND_URL + "/platform-auth-success");
    }

    @GetMapping("/connect/youtubemusic")
    public void connectYoutubemusic(
            @RequestParam(value = "jwt_token", required = false) String token,
            HttpServletResponse response) throws IOException {

        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("youtubemusic");
        String state = stateManager.generateState();

        if (token != null && !token.isBlank()) {
            try {
                String username = jwtService.extractUsername(token);
                User user = (User) userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(token, user)) {
                    stateManager.putUser(state, user.getId());
                }
            } catch (Exception ignored) {
            }
        }

        String authUri = UriComponentsBuilder
                .fromUriString(registration.getProviderDetails().getAuthorizationUri())
                .queryParam("client_id", registration.getClientId())
                .queryParam("response_type", "code")
                .queryParam("scope", String.join(" ", registration.getScopes()))
                .queryParam("redirect_uri", registration.getRedirectUri().replace("${BASE_URL}", BASE_URL))
                .queryParam("state", state)
                .queryParam("access_type", "offline")
                .build()
                .toUriString();

        response.sendRedirect(authUri);
    }

    @GetMapping("/callback/youtubemusic")
    public void youtubemusicCallback(@RequestParam("code") String code,
                                     @RequestParam("state") String state,
                                     HttpServletResponse response) throws IOException {

        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("youtubemusic");

        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri(registration.getProviderDetails().getAuthorizationUri())
                .clientId(registration.getClientId())
                .redirectUri(registration.getRedirectUri().replace("${BASE_URL}", BASE_URL))
                .scopes(registration.getScopes())
                .state(state)
                .build();

        OAuth2AuthorizationResponse authorizationResponse = OAuth2AuthorizationResponse.success(code)
                .redirectUri(registration.getRedirectUri().replace("${BASE_URL}", BASE_URL))
                .build();

        OAuth2AuthorizationCodeGrantRequest grantRequest = new OAuth2AuthorizationCodeGrantRequest(
                registration,
                new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse));

        OAuth2AccessTokenResponse tokenResponse = new DefaultAuthorizationCodeTokenResponseClient()
                .getTokenResponse(grantRequest);

        Long userId = stateManager.removeUser(state);

        if (userId != null) {
            oauthTokenService.save(
                    userId,
                    StreamingPlatform.YOUTUBEMUSIC,
                    tokenResponse.getAccessToken().getTokenValue(),
                    tokenResponse.getRefreshToken() != null ? tokenResponse.getRefreshToken().getTokenValue() : null,
                    tokenResponse.getAccessToken().getExpiresAt());
        } else {
            stateManager.putTempToken(state, tokenResponse);
        }

        response.sendRedirect(FRONTEND_URL + "/platform-auth-success");
    }

    @GetMapping("/connect/soundcloud")
    public void connectSoundCloud(
            @RequestParam(value = "jwt_token", required = false) String token,
            HttpServletResponse response) throws IOException {

        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("soundcloud");
        String state = stateManager.generateState();

        if (token != null && !token.isBlank()) {
            try {
                String username = jwtService.extractUsername(token);
                User user = (User) userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(token, user)) {
                    stateManager.putUser(state, user.getId());
                }
            } catch (Exception ignored) {
            }
        }

        // PKCE: generate code verifier + challenge
        String codeVerifier = PKCEUtil.generateCodeVerifier();
        String codeChallenge = PKCEUtil.generateCodeChallenge(codeVerifier);
        stateManager.putCodeVerifier(state, codeVerifier);

        String authUri = UriComponentsBuilder
                .fromUriString(registration.getProviderDetails().getAuthorizationUri())
                .queryParam("client_id", registration.getClientId())
                .queryParam("redirect_uri", registration.getRedirectUri().replace("${BASE_URL}", BASE_URL))
                .queryParam("response_type", "code")
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .queryParam("state", state)
                .build()
                .toUriString();

        response.sendRedirect(authUri);
    }

    @GetMapping("/callback/soundcloud")
    public void soundCloudCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletResponse response) throws IOException {

        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("soundcloud");

        // retrieve code verifier
        String codeVerifier = stateManager.removeCodeVerifier(state);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", registration.getClientId());
        form.add("client_secret", registration.getClientSecret());
        form.add("redirect_uri", registration.getRedirectUri().replace("${BASE_URL}", BASE_URL));
        form.add("code_verifier", codeVerifier);
        form.add("code", code);

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> tokenResponse = restTemplate.postForObject(
                "https://secure.soundcloud.com/oauth/token",
                new HttpEntity<>(form, createFormHeaders()),
                Map.class);
        OAuth2AccessTokenResponse accessTokenResponse = OAuth2AccessTokenResponse.withToken((String) tokenResponse.get("access_token"))
                .tokenType(OAuth2AccessToken.TokenType.BEARER)
                .refreshToken((String) tokenResponse.get("refresh_token"))
                .expiresIn(((Number) tokenResponse.get("expires_in")).longValue())
                .build();

        Long userId = stateManager.removeUser(state);

        if (userId != null) {
            oauthTokenService.save(
                    userId,
                    StreamingPlatform.SOUNDCLOUD,
                    accessTokenResponse.getAccessToken().getTokenValue(),
                    accessTokenResponse.getRefreshToken() != null ? accessTokenResponse.getRefreshToken().getTokenValue() : null,
                    accessTokenResponse.getAccessToken().getExpiresAt()
            );
        } else {
            stateManager.putTempToken(state, accessTokenResponse);
        }

        response.sendRedirect(FRONTEND_URL + "/platform-auth-success");
    }

    // helper method to create form headers
    private HttpHeaders createFormHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }


    @GetMapping("/connect/spotify")
    public void connectSpotify(
            @RequestParam(value = "jwt_token", required = false) String token,
            HttpServletResponse response) throws IOException {

        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("spotify");
        String state = stateManager.generateState();

        if (token != null && !token.isBlank()) {
            try {
                String username = jwtService.extractUsername(token);
                User user = (User) userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(token, user)) {
                    stateManager.putUser(state, user.getId());
                }
            } catch (Exception ignored) {
            }
        }

        String authUri = UriComponentsBuilder
                .fromUriString(registration.getProviderDetails().getAuthorizationUri())
                .queryParam("client_id", registration.getClientId())
                .queryParam("response_type", "code")
                .queryParam("scope", String.join(" ", registration.getScopes()))
                .queryParam("redirect_uri", registration.getRedirectUri().replace("${BASE_URL}", BASE_URL))
                .queryParam("state", state)
                .queryParam("show_dialog", "true") // optional: forces Spotify login screen
                .build()
                .toUriString();

        response.sendRedirect(authUri);
    }


    @GetMapping("/callback/spotify")
    public void spotifyCallback(@RequestParam("code") String code,
                                @RequestParam("state") String state,
                                HttpServletResponse response) throws IOException {

        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("spotify");

        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri(registration.getProviderDetails().getAuthorizationUri())
                .clientId(registration.getClientId())
                .redirectUri(registration.getRedirectUri().replace("${BASE_URL}", BASE_URL))
                .scopes(registration.getScopes())
                .state(state)
                .build();

        OAuth2AuthorizationResponse authorizationResponse = OAuth2AuthorizationResponse.success(code)
                .redirectUri(registration.getRedirectUri().replace("${BASE_URL}", BASE_URL))
                .build();

        OAuth2AuthorizationCodeGrantRequest grantRequest = new OAuth2AuthorizationCodeGrantRequest(
                registration,
                new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse));

        OAuth2AccessTokenResponse tokenResponse = new DefaultAuthorizationCodeTokenResponseClient()
                .getTokenResponse(grantRequest);

        Long userId = stateManager.removeUser(state);

        if (userId != null) {
            oauthTokenService.save(
                    userId,
                    StreamingPlatform.SPOTIFY,
                    tokenResponse.getAccessToken().getTokenValue(),
                    tokenResponse.getRefreshToken() != null ? tokenResponse.getRefreshToken().getTokenValue() : null,
                    tokenResponse.getAccessToken().getExpiresAt());
        } else {
            stateManager.putTempToken(state, tokenResponse);
        }

        response.sendRedirect(FRONTEND_URL + "/platform-auth-success");
    }

}
