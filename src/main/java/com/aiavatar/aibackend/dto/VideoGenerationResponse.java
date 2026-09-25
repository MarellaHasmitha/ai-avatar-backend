package com.aiavatar.aibackend.dto;

import com.aiavatar.aibackend.entity.VideoGenerationStatus;

import java.time.LocalDateTime;

public class VideoGenerationResponse {

    private Long id;
    private Long projectId;
    private String prompt;
    private VideoGenerationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public VideoGenerationResponse() {
    }

    public VideoGenerationResponse(
            Long id,
            Long projectId,
            String prompt,
            VideoGenerationStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.id = id;
        this.projectId = projectId;
        this.prompt = prompt;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getPrompt() {
        return prompt;
    }

    public VideoGenerationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}