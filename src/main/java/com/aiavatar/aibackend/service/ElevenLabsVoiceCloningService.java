package com.aiavatar.aibackend.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@ConditionalOnProperty(
        name = "tts.provider",
        havingValue = "elevenlabs"
)
public class ElevenLabsVoiceCloningService {

    private final RestClient elevenLabsRestClient;

    public ElevenLabsVoiceCloningService(
            RestClient elevenLabsRestClient) {

        this.elevenLabsRestClient = elevenLabsRestClient;
    }

    public String cloneVoice(
            String voiceName,
            MultipartFile audioFile) {

        if (voiceName == null || voiceName.isBlank()) {
            throw new IllegalArgumentException(
                    "Voice name cannot be empty"
            );
        }

        if (audioFile == null || audioFile.isEmpty()) {
            throw new IllegalArgumentException(
                    "Audio file cannot be empty"
            );
        }

        try {

            ByteArrayResource audioResource =
                    new ByteArrayResource(audioFile.getBytes()) {

                        @Override
                        public String getFilename() {
                            return audioFile.getOriginalFilename();
                        }
                    };

            MultiValueMap<String, Object> formData =
                    new LinkedMultiValueMap<>();

            formData.add("name", voiceName);
            formData.add("files", audioResource);

            Map<?, ?> response =
                    elevenLabsRestClient
                            .post()
                            .uri("/v1/voices/add")
                            .contentType(
                                    MediaType.MULTIPART_FORM_DATA
                            )
                            .body(formData)
                            .retrieve()
                            .body(Map.class);

            if (response == null ||
                    response.get("voice_id") == null) {

                throw new RuntimeException(
                        "ElevenLabs did not return a voice ID"
                );
            }

            return response
                    .get("voice_id")
                    .toString();

        } catch (RestClientResponseException e) {

            System.err.println(
                    "========== ELEVENLABS ERROR =========="
            );

            System.err.println(
                    "HTTP Status: " + e.getStatusCode()
            );

            System.err.println(
                    "Response Body: " + e.getResponseBodyAsString()
            );

            System.err.println(
                    "======================================="
            );

            throw new RuntimeException(
                    "ElevenLabs API error: "
                            + e.getResponseBodyAsString(),
                    e
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to read voice sample",
                    e
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to clone voice using ElevenLabs",
                    e
            );
        }
    }
}