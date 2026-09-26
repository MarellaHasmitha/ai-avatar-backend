package com.aiavatar.aibackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@ConditionalOnProperty(
        name = "tts.provider",
        havingValue = "elevenlabs"
)
public class ElevenLabsConfig {

    @Bean
    public RestClient elevenLabsRestClient(
            @Value("${elevenlabs.api-key}") String apiKey) {

        return RestClient.builder()
                .baseUrl("https://api.elevenlabs.io")
                .defaultHeader("xi-api-key", apiKey)
                .build();
    }
}