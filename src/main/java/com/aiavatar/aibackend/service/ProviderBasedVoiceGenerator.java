package com.aiavatar.aibackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProviderBasedVoiceGenerator implements VoiceGenerator {

    private final CartesiaVoiceGenerator cartesiaVoiceGenerator;
    private final ElevenLabsVoiceGenerator elevenLabsVoiceGenerator;
    private final String provider;

    public ProviderBasedVoiceGenerator(
            CartesiaVoiceGenerator cartesiaVoiceGenerator,
            ElevenLabsVoiceGenerator elevenLabsVoiceGenerator,
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