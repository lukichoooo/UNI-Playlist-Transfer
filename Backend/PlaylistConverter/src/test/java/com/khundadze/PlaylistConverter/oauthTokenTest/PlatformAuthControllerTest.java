package com.khundadze.PlaylistConverter.securityConfig.oauth2ForPlatformAuth;

import com.khundadze.PlaylistConverter.enums.StreamingPlatform;
import com.khundadze.PlaylistConverter.models_db.User;
import com.khundadze.PlaylistConverter.securityConfig.JwtService;
import com.khundadze.PlaylistConverter.services.OAuthTokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlatformAuthControllerTest {

    @Mock
    private ClientRegistrationRepository clientRegistrationRepository;

    @Mock
    private OAuthTokenService oauthTokenService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private StateManager stateManager;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> tokenResponseClient;

    @InjectMocks
    private PlatformAuthController controller;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "BASE_URL", "http://localhost:8080");
        ReflectionTestUtils.setField(controller, "FRONTEND_URL", "http://localhost:3000");
        ReflectionTestUtils.setField(controller, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(controller, "tokenResponseClient", tokenResponseClient);
    }

    private void invokeProcessTokenResponse(String state, OAuth2AccessTokenResponse tokenResponse, StreamingPlatform platform) {
        ReflectionTestUtils.invokeMethod(controller, "processTokenResponse", state, tokenResponse, platform);
    }

    private void invokeHandleUserAuthentication(String token, String state) {
        ReflectionTestUtils.invokeMethod(controller, "handleUserAuthentication", token, state);
    }

    @Test
    void connectToPlatform_shouldReturnBadRequestForUnknownPlatform() throws IOException {
        String platform = "unknown";
        when(clientRegistrationRepository.findByRegistrationId(platform)).thenReturn(null);

        controller.connectToPlatform(platform, null, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown platform: " + platform);
    }

    @Test
    void handleUserAuthentication_forRegisteredUser_shouldPutPrincipalInStateManager() {
        String token = "mock-jwt-token";
        String state = "mockState";
        String principalId = "testuser";
        Claims claims = mock(Claims.class);
        User user = new User();
        user.setId(123L);
        when(jwtService.extractAllClaims(token)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(principalId);
        when(claims.get("auth", List.class)).thenReturn(List.of("ROLE_USER"));
        when(userDetailsService.loadUserByUsername(principalId)).thenReturn(user);

        invokeHandleUserAuthentication(token, state);

        verify(stateManager).putPrincipal(state, user.getId(), true);
    }

    @Test
    void handleUserAuthentication_forGuestUser_shouldPutPrincipalInStateManager() {
        String token = "mock-jwt-token";
        String state = "mockState";
        String principalId = "guest:guest123";
        Claims claims = mock(Claims.class);
        when(jwtService.extractAllClaims(token)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(principalId);
        when(claims.get("auth", List.class)).thenReturn(List.of("ROLE_ANONYMOUS"));

        invokeHandleUserAuthentication(token, state);

        verify(stateManager).putPrincipal(state, principalId, false);
    }

    @Test
    void handleUserAuthentication_withInvalidToken_shouldDoNothing() {
        String token = "invalid-jwt-token";
        String state = "mockState";
        when(jwtService.extractAllClaims(token)).thenThrow(new IllegalArgumentException("Invalid token"));

        invokeHandleUserAuthentication(token, state);

        verify(stateManager, times(0)).putPrincipal(anyString(), any(), anyBoolean());
    }

    @Test
    void processTokenResponse_forRegisteredUser_shouldSaveTokenCorrectly() {
        String state = "testState";
        Long userId = 1L;
        StreamingPlatform platform = StreamingPlatform.SPOTIFY;
        String principalValue = "user:" + userId;

        OAuth2AccessTokenResponse tokenResponse = OAuth2AccessTokenResponse.withToken("testAccessToken")
                .tokenType(OAuth2AccessToken.TokenType.BEARER)
                .expiresIn(3600L)
                .refreshToken("testRefreshToken")
                .scopes(Set.of("scope1", "scope2"))
                .build();

        when(stateManager.removePrincipal(state)).thenReturn(principalValue);
        when(stateManager.isRegisteredUser(principalValue)).thenReturn(true);

        invokeProcessTokenResponse(state, tokenResponse, platform);

        verify(oauthTokenService).saveForRegisteredUser(
                userId,
                platform,
                "testAccessToken",
                "testRefreshToken",
                tokenResponse.getAccessToken().getExpiresAt()
        );
    }

    @Test
    void processTokenResponse_forGuestUser_shouldSaveTokenCorrectly() {
        String state = "testState";
        String guestId = "guest123";
        StreamingPlatform platform = StreamingPlatform.YOUTUBEMUSIC;
        String principalValue = "guest:" + guestId;

        OAuth2AccessTokenResponse tokenResponse = OAuth2AccessTokenResponse.withToken("guestAccessToken")
                .tokenType(OAuth2AccessToken.TokenType.BEARER)
                .expiresIn(3600L)
                .refreshToken(null)
                .scopes(Set.of("scope3"))
                .build();

        when(stateManager.removePrincipal(state)).thenReturn(principalValue);
        when(stateManager.isRegisteredUser(principalValue)).thenReturn(false);

        invokeProcessTokenResponse(state, tokenResponse, platform);

        verify(oauthTokenService).saveForGuest(
                guestId,
                platform,
                "guestAccessToken",
                null,
                tokenResponse.getAccessToken().getExpiresAt()
        );
    }
}
