package com.aiavatar.aibackend.service;

public interface VoiceGenerator {

    byte[] generateVoice(String text, String voiceId);
}