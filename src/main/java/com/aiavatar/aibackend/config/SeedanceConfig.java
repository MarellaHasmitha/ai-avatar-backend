package com.aiavatar.aibackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class SeedanceConfig {

    @Bean
    public RestClient seedanceRestClient(
            @Value("${seedance.api-key}") String apiKey) {

        return RestClient.builder()
                .baseUrl("https://seedanceapi.org/v2")
                .defaultHeader(
                        "Authorization",
                        "Bearer " + apiKey
                )
                .defaultHeader(
                        "Content-Type",
                        "application/json"
                )
                .build();
    }
}