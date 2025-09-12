package com.khundadze.PlaylistConverter.securityConfig.oauth2ForPlatformAuth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OAuth2ClientPlatformConfig {
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        List<ClientRegistration> registrations = new ArrayList<>();
        registrations.add(youtubeClientRegistration());
        return new InMemoryClientRegistrationRepository(registrations);
    }

    @Value("${spring.security.oauth2.client.registration.youtube.client-id}")
    private String youtubeClientId;

    @Value("${spring.security.oauth2.client.registration.youtube.client-secret}")
    private String youtubeClientSecret;

    private ClientRegistration youtubeClientRegistration() {
        return ClientRegistration.withRegistrationId("youtube")
                .clientId(youtubeClientId)
                .clientSecret(youtubeClientSecret)
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
