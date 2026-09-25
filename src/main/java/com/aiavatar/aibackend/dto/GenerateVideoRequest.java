package com.aiavatar.aibackend.dto;

import jakarta.validation.constraints.NotBlank;

public class GenerateVideoRequest {

    @NotBlank(message = "Prompt is required")
    private String prompt;

    public GenerateVideoRequest() {
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}