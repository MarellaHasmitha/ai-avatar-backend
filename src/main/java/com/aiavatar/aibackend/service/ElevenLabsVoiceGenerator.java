package com.aiavatar.aibackend.service;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class ElevenLabsVoiceGenerator {

    private final RestClient elevenLabsRestClient;

    public ElevenLabsVoiceGenerator(RestClient elevenLabsRestClient) {
        this.elevenLabsRestClient = elevenLabsRestClient;
    }

    public byte[] generateVoice(String text, String voiceId) {

        // 1. Validate text
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text cannot be empty");
        }

        // 2. Validate voice ID
        if (voiceId == null || voiceId.isBlank()) {
            throw new IllegalArgumentException("Voice ID cannot be empty");
        }

        // 3. ElevenLabs request body
        Map<String, Object> requestBody = Map.of(
                "text", text,
                "model_id", "eleven_multilingual_v2"
        );

        try {

            // 4. Call ElevenLabs Text-to-Speech API
            byte[] audioBytes = elevenLabsRestClient
                    .post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/text-to-speech/{voiceId}")
                            .queryParam("output_format", "mp3_44100_128")
                            .build(voiceId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.parseMediaType("audio/mpeg"))
                    .body(requestBody)
                    .retrieve()
                    .body(byte[].class);

            // 5. Make sure ElevenLabs returned audio
            if (audioBytes == null || audioBytes.length == 0) {
                throw new RuntimeException(
                        "ElevenLabs returned empty audio"
                );
            }

            System.out.println(
                    "ElevenLabs voice generated successfully. Audio size: "
                    + audioBytes.length + " bytes"
            );

            return audioBytes;

        } catch (RestClientResponseException e) {

            // Print the actual ElevenLabs error
            System.out.println("========== ELEVENLABS ERROR ==========");
            System.out.println("Status: " + e.getStatusCode());
            System.out.println(
                    "Response: " + e.getResponseBodyAsString()
            );
            System.out.println("======================================");

            throw new RuntimeException(
                    "ElevenLabs API error: "
                            + e.getResponseBodyAsString(),
                    e
            );

        } catch (Exception e) {

            // Print unexpected errors
            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to generate voice using ElevenLabs: "
                            + e.getMessage(),
                    e
            );
        }
    }
}