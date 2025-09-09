package com.khundadze.PlaylistConverter.securityConfig.oauth2ForPlatformAuth;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.khundadze.PlaylistConverter.enums.StreamingPlatform;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2DispatcherSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuthStreamingPlatformTokenHandler streamingHandler;
    private final OAuth2LoginSuccessHandler loginHandler;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            // fallback, just use login handler
            loginHandler.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        // check if this is a streaming platform
        StreamingPlatform platform = StreamingPlatform.fromStringSafe(oauthToken.getAuthorizedClientRegistrationId());

        if (platform != null) {
            // streaming OAuth → save token
            streamingHandler.onAuthenticationSuccess(request, response, authentication);
        } else {
            // normal login (Google/GitHub)
            loginHandler.onAuthenticationSuccess(request, response, authentication);
        }
    }
}
