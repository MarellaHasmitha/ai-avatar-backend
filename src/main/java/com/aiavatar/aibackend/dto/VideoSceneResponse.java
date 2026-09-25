package com.aiavatar.aibackend.dto;

import com.aiavatar.aibackend.entity.SceneProcessingStatus;

public class VideoSceneResponse {

    private Long sceneId;
    private Integer sceneNumber;
    private Integer duration;

    private String narration;
    private String visualPrompt;

    private boolean avatarRequired;

    private SceneProcessingStatus voiceStatus;
    private String audioUrl;

    private SceneProcessingStatus visualStatus;
    private String visualUrl;

    private SceneProcessingStatus avatarStatus;
    private String avatarVideoUrl;

    private SceneProcessingStatus videoStatus;
    private String sceneVideoUrl;

    public VideoSceneResponse() {
    }

    public VideoSceneResponse(
            Long sceneId,
            Integer sceneNumber,
            Integer duration,
            String narration,
            String visualPrompt,
            boolean avatarRequired,
            SceneProcessingStatus voiceStatus,
            String audioUrl,
            SceneProcessingStatus visualStatus,
            String visualUrl,
            SceneProcessingStatus avatarStatus,
            String avatarVideoUrl,
            SceneProcessingStatus videoStatus,
            String sceneVideoUrl) {

        this.sceneId = sceneId;
        this.sceneNumber = sceneNumber;
        this.duration = duration;
        this.narration = narration;
        this.visualPrompt = visualPrompt;
        this.avatarRequired = avatarRequired;
        this.voiceStatus = voiceStatus;
        this.audioUrl = audioUrl;
        this.visualStatus = visualStatus;
        this.visualUrl = visualUrl;
        this.avatarStatus = avatarStatus;
        this.avatarVideoUrl = avatarVideoUrl;
        this.videoStatus = videoStatus;
        this.sceneVideoUrl = sceneVideoUrl;
    }

    public Long getSceneId() {
        return sceneId;
    }

    public void setSceneId(Long sceneId) {
        this.sceneId = sceneId;
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

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public SceneProcessingStatus getVisualStatus() {
        return visualStatus;
    }

    public void setVisualStatus(SceneProcessingStatus visualStatus) {
        this.visualStatus = visualStatus;
    }

    public String getVisualUrl() {
        return visualUrl;
    }

    public void setVisualUrl(String visualUrl) {
        this.visualUrl = visualUrl;
    }

    public SceneProcessingStatus getAvatarStatus() {
        return avatarStatus;
    }

    public void setAvatarStatus(SceneProcessingStatus avatarStatus) {
        this.avatarStatus = avatarStatus;
    }

    public String getAvatarVideoUrl() {
        return avatarVideoUrl;
    }

    public void setAvatarVideoUrl(String avatarVideoUrl) {
        this.avatarVideoUrl = avatarVideoUrl;
    }

    public SceneProcessingStatus getVideoStatus() {
        return videoStatus;
    }

    public void setVideoStatus(SceneProcessingStatus videoStatus) {
        this.videoStatus = videoStatus;
    }

    public String getSceneVideoUrl() {
        return sceneVideoUrl;
    }

    public void setSceneVideoUrl(String sceneVideoUrl) {
        this.sceneVideoUrl = sceneVideoUrl;
    }
}