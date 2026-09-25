package com.aiavatar.aibackend.dto;

import jakarta.validation.constraints.NotBlank;

public class GenerateVoiceRequest {

    @NotBlank(message = "Voice ID is required")
    private String voiceId;

    public GenerateVoiceRequest() {
    }

    public String getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }
}