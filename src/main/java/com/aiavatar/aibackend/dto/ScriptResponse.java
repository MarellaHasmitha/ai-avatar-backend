package com.aiavatar.aibackend.dto;

import java.time.LocalDateTime;

public class ScriptResponse {

    private Long id;
    private String content;
    private Long projectId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ScriptResponse(
            Long id,
            String content,
            Long projectId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.id = id;
        this.content = content;
        this.projectId = projectId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public Long getProjectId() {
        return projectId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}