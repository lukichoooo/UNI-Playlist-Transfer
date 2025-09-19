package com.khundadze.PlaylistConverter.streamingServices;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient spotifyWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://api.spotify.com/v1")
                .build();
    }

    @Bean
    public WebClient soundCloudWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://api.soundcloud.com")
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs()
                                .maxInMemorySize(16 * 1024 * 1024)) // 16 MB
                        .build())
                .build();
    }

    @Bean
    public WebClient youTubeWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://www.googleapis.com/youtube/v3")
                .build();
    }

}
