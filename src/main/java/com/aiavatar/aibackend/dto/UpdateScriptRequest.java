package com.aiavatar.aibackend.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateScriptRequest {

    @NotBlank(message = "Script content is required")
    private String content;

    public UpdateScriptRequest() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}