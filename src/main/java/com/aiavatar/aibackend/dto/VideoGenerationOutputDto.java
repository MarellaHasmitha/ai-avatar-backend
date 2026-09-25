package com.aiavatar.aibackend.dto;

public class VideoGenerationOutputDto {

    private Long videoGenerationId;
    private Long sceneId;
    private Integer sceneNumber;

    private String status;

    private Double audioDuration;
    private Double pixazoDuration;

    private String audioUrl;
    private String visualVideoUrl;
    private String sceneVideoUrl;

    private Boolean audioIncluded;

    private String pixazoRequestId;

    private String message;

    public VideoGenerationOutputDto() {
    }

    public VideoGenerationOutputDto(
            Long videoGenerationId,
            Long sceneId,
            Integer sceneNumber,
            String status,
            Double audioDuration,
            Double pixazoDuration,
            String audioUrl,
            String visualVideoUrl,
            String sceneVideoUrl,
            Boolean audioIncluded,
            String pixazoRequestId,
            String message) {

        this.videoGenerationId = videoGenerationId;
        this.sceneId = sceneId;
        this.sceneNumber = sceneNumber;
        this.status = status;
        this.audioDuration = audioDuration;
        this.pixazoDuration = pixazoDuration;
        this.audioUrl = audioUrl;
        this.visualVideoUrl = visualVideoUrl;
        this.sceneVideoUrl = sceneVideoUrl;
        this.audioIncluded = audioIncluded;
        this.pixazoRequestId = pixazoRequestId;
        this.message = message;
    }

    public Long getVideoGenerationId() {
        return videoGenerationId;
    }

    public void setVideoGenerationId(Long videoGenerationId) {
        this.videoGenerationId = videoGenerationId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getAudioDuration() {
        return audioDuration;
    }

    public void setAudioDuration(Double audioDuration) {
        this.audioDuration = audioDuration;
    }

    public Double getPixazoDuration() {
        return pixazoDuration;
    }

    public void setPixazoDuration(Double pixazoDuration) {
        this.pixazoDuration = pixazoDuration;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getVisualVideoUrl() {
        return visualVideoUrl;
    }

    public void setVisualVideoUrl(String visualVideoUrl) {
        this.visualVideoUrl = visualVideoUrl;
    }

    public String getSceneVideoUrl() {
        return sceneVideoUrl;
    }

    public void setSceneVideoUrl(String sceneVideoUrl) {
        this.sceneVideoUrl = sceneVideoUrl;
    }

    public Boolean getAudioIncluded() {
        return audioIncluded;
    }

    public void setAudioIncluded(Boolean audioIncluded) {
        this.audioIncluded = audioIncluded;
    }

    public String getPixazoRequestId() {
        return pixazoRequestId;
    }

    public void setPixazoRequestId(String pixazoRequestId) {
        this.pixazoRequestId = pixazoRequestId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}