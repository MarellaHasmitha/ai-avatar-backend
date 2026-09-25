package com.aiavatar.aibackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CartesiaConfig {

    @Bean
    public RestClient cartesiaRestClient(
            @Value("${cartesia.api-key}") String apiKey) {

        return RestClient.builder()
                .baseUrl("https://api.cartesia.ai")
                .defaultHeader("X-API-Key", apiKey)
                .defaultHeader("Cartesia-Version", "2026-03-01")
                .build();
    }
}