package com.aiavatar.aibackend.service;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class CartesiaVoiceGenerator {

    private final RestClient cartesiaRestClient;

    public CartesiaVoiceGenerator(RestClient cartesiaRestClient) {
        this.cartesiaRestClient = cartesiaRestClient;
    }

    public byte[] generateVoice(String text, String voiceId) {

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text cannot be empty");
        }

        if (voiceId == null || voiceId.isBlank()) {
            throw new IllegalArgumentException("Voice ID cannot be empty");
        }

        Map<String, Object> voice = Map.of(
                "mode", "id",
                "id", voiceId
        );

        /*
         * Request compressed MP3 audio from Cartesia.
         */
        Map<String, Object> outputFormat = Map.of(
                "container", "mp3",
                "sample_rate", 44100,
                "bit_rate", 128000
        );
        
        Map<String, Object> requestBody = Map.of(
                "model_id", "sonic-3.5",
                "transcript", text,
                "voice", voice,
                "language", "en",
                "output_format", outputFormat
        );

        try {

            byte[] audioBytes = cartesiaRestClient
                    .post()
                    .uri("/tts/bytes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.parseMediaType("audio/mpeg"))
                    .body(requestBody)
                    .retrieve()
                    .body(byte[].class);

            if (audioBytes == null || audioBytes.length == 0) {
                throw new RuntimeException(
                        "Cartesia returned empty audio"
                );
            }

            System.out.println(
                    "Cartesia MP3 voice generated successfully. Audio size: "
                    + audioBytes.length + " bytes"
            );

            return audioBytes;

        } catch (RestClientResponseException e) {

            System.out.println("========== CARTESIA ERROR ==========");
            System.out.println("Status: " + e.getStatusCode());
            System.out.println(
                    "Response: " + e.getResponseBodyAsString()
            );
            System.out.println("====================================");

            throw new RuntimeException(
                    "Cartesia API error: "
                            + e.getResponseBodyAsString(),
                    e
            );

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to generate voice using Cartesia: "
                            + e.getMessage(),
                    e
            );
     }
    }
}