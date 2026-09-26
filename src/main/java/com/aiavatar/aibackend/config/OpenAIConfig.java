package com.aiavatar.aibackend.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {

    @Bean
    public OpenAIClient openAIClient() {

        String apiKey = System.getenv("OPENROUTER_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "OPENROUTER_API_KEY environment variable is not set"
            );
        }

        return OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .baseUrl("https://openrouter.ai/api/v1")
                .build();
    }
}