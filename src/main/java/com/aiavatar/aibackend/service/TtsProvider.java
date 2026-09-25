package com.aiavatar.aibackend.service;

public interface TtsProvider {

    byte[] generateVoice(String text, String voiceId);

}