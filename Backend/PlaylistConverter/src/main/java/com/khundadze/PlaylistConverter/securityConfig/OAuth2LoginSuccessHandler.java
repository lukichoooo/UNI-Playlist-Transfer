package com.khundadze.PlaylistConverter.securityConfig;

import java.io.IOException;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.khundadze.PlaylistConverter.authenticationMVC.AuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService; // your AuthService
    private final String frontendUrl = "http://localhost:5173";

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            response.sendRedirect(frontendUrl + "/login");
            return;
        }

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        Map<String, Object> userAttributes = oauthToken.getPrincipal().getAttributes();

        // Safely extract username
        String username = extractUsername(userAttributes);

        // Generate JWT via AuthService
        String token = authService.oauthLogin(username);

        // Redirect to frontend with token
        response.sendRedirect(frontendUrl + "/oauth-success?token=" + token);
    }

    private String extractUsername(Map<String, Object> attributes) {
        if (attributes.containsKey("email") && attributes.get("email") != null) {
            return attributes.get("email").toString();
        } else if (attributes.containsKey("login") && attributes.get("login") != null) {
            return attributes.get("login").toString();
        } else if (attributes.containsKey("name") && attributes.get("name") != null) {
            return attributes.get("name").toString();
        } else if (attributes.containsKey("id") && attributes.get("id") != null) {
            return "user_" + attributes.get("id").toString();
        } else {
            return "anonymous_user_" + System.currentTimeMillis();
        }
    }
}
