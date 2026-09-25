package com.aiavatar.aibackend.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateScriptRequest {

    @NotBlank(message = "Script content is required")
    private String content;

    public CreateScriptRequest() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}