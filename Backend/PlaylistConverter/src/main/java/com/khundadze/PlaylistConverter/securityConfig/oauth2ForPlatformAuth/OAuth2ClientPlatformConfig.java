package com.khundadze.PlaylistConverter.securityConfig.oauth2ForPlatformAuth;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

public class OAuth2ClientPlatformConfig {
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        List<ClientRegistration> registrations = new ArrayList<>();
        registrations.add(youtubeClientRegistration());
        return new InMemoryClientRegistrationRepository(registrations);
    }

    private ClientRegistration youtubeClientRegistration() {
        return ClientRegistration.withRegistrationId("youtube")
                .clientId("YOUR_GOOGLE_CLIENT_ID")
                .clientSecret("YOUR_GOOGLE_CLIENT_SECRET")
                .scope("https://www.googleapis.com/auth/youtube.readonly",
                        "https://www.googleapis.com/auth/youtube")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/api/platformAuth/callback/youtube")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .build();
    }

}
