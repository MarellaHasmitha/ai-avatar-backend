package com.aiavatar.aibackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class ProviderBasedVoiceGenerator implements VoiceGenerator {

    private final CartesiaVoiceGenerator cartesiaVoiceGenerator;
    private final ElevenLabsVoiceGenerator elevenLabsVoiceGenerator;
    private final String provider;

    public ProviderBasedVoiceGenerator(
            CartesiaVoiceGenerator cartesiaVoiceGenerator,
            @Nullable ElevenLabsVoiceGenerator elevenLabsVoiceGenerator,
            @Value("${tts.provider}") String provider) {

        this.cartesiaVoiceGenerator = cartesiaVoiceGenerator;
        this.elevenLabsVoiceGenerator = elevenLabsVoiceGenerator;
        this.provider = provider;
    }

    @Override
    public byte[] generateVoice(String text, String voiceId) {

        if ("cartesia".equalsIgnoreCase(provider)) {

            return cartesiaVoiceGenerator.generateVoice(
                    text,
                    voiceId
            );
        }

        if ("elevenlabs".equalsIgnoreCase(provider)) {

            if (elevenLabsVoiceGenerator == null) {
                throw new IllegalStateException(
                        "ElevenLabs provider is selected, "
                                + "but ElevenLabsVoiceGenerator is not available. "
                                + "Please configure ELEVENLABS_API_KEY."
                );
            }

            return elevenLabsVoiceGenerator.generateVoice(
                    text,
                    voiceId
            );
        }

        throw new IllegalArgumentException(
                "Unsupported TTS provider: " + provider
        );
    }
}