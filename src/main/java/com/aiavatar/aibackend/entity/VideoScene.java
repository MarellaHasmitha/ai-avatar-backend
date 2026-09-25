package com.aiavatar.aibackend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "video_scenes")
public class VideoScene {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_generation_id", nullable = false)
    private VideoGeneration videoGeneration;

    @Column(name = "scene_number", nullable = false)
    private Integer sceneNumber;

    @Column(nullable = false)
    private Integer duration;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String narration;

    @Column(name = "visual_prompt", nullable = false, columnDefinition = "TEXT")
    private String visualPrompt;

    @Column(name = "avatar_required", nullable = false)
    private boolean avatarRequired;

    @Enumerated(EnumType.STRING)
    @Column(name = "voice_status", nullable = false)
    private SceneProcessingStatus voiceStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "visual_status", nullable = false)
    private SceneProcessingStatus visualStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "avatar_status", nullable = false)
    private SceneProcessingStatus avatarStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "video_status", nullable = false)
    private SceneProcessingStatus videoStatus;

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(name = "visual_url")
    private String visualUrl;

    @Column(name = "avatar_video_url")
    private String avatarVideoUrl;

    @Column(name = "scene_video_url")
    private String sceneVideoUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (voiceStatus == null) {
            voiceStatus = SceneProcessingStatus.PENDING;
        }

        if (visualStatus == null) {
            visualStatus = SceneProcessingStatus.PENDING;
        }

        if (avatarStatus == null) {
            avatarStatus = SceneProcessingStatus.PENDING;
        }

        if (videoStatus == null) {
            videoStatus = SceneProcessingStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public VideoScene() {
    }

    public Long getId() {
        return id;
    }

    public VideoGeneration getVideoGeneration() {
        return videoGeneration;
    }

    public void setVideoGeneration(VideoGeneration videoGeneration) {
        this.videoGeneration = videoGeneration;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSceneNumber() {
        return sceneNumber;
    }

    public void setSceneNumber(Integer sceneNumber) {
        this.sceneNumber = sceneNumber;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public String getVisualPrompt() {
        return visualPrompt;
    }

    public void setVisualPrompt(String visualPrompt) {
        this.visualPrompt = visualPrompt;
    }

    public boolean isAvatarRequired() {
        return avatarRequired;
    }

    public void setAvatarRequired(boolean avatarRequired) {
        this.avatarRequired = avatarRequired;
    }

    public SceneProcessingStatus getVoiceStatus() {
        return voiceStatus;
    }

    public void setVoiceStatus(SceneProcessingStatus voiceStatus) {
        this.voiceStatus = voiceStatus;
    }

    public SceneProcessingStatus getVisualStatus() {
        return visualStatus;
    }

    public void setVisualStatus(SceneProcessingStatus visualStatus) {
        this.visualStatus = visualStatus;
    }

    public SceneProcessingStatus getAvatarStatus() {
        return avatarStatus;
    }

    public void setAvatarStatus(SceneProcessingStatus avatarStatus) {
        this.avatarStatus = avatarStatus;
    }

    public SceneProcessingStatus getVideoStatus() {
        return videoStatus;
    }

    public void setVideoStatus(SceneProcessingStatus videoStatus) {
        this.videoStatus = videoStatus;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getVisualUrl() {
        return visualUrl;
    }

    public void setVisualUrl(String visualUrl) {
        this.visualUrl = visualUrl;
    }

    public String getAvatarVideoUrl() {
        return avatarVideoUrl;
    }

    public void setAvatarVideoUrl(String avatarVideoUrl) {
        this.avatarVideoUrl = avatarVideoUrl;
    }

    public String getSceneVideoUrl() {
        return sceneVideoUrl;
    }

    public void setSceneVideoUrl(String sceneVideoUrl) {
        this.sceneVideoUrl = sceneVideoUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}