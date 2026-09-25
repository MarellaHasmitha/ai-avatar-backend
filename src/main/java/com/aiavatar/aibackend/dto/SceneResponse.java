package com.aiavatar.aibackend.dto;

public class SceneResponse {

    private int sceneNumber;
    private int duration;
    private String narration;
    private String visualPrompt;
    private boolean avatarRequired;

    public SceneResponse() {
    }

    public SceneResponse(
            int sceneNumber,
            int duration,
            String narration,
            String visualPrompt,
            boolean avatarRequired) {

        this.sceneNumber = sceneNumber;
        this.duration = duration;
        this.narration = narration;
        this.visualPrompt = visualPrompt;
        this.avatarRequired = avatarRequired;
    }

    public int getSceneNumber() {
        return sceneNumber;
    }

    public void setSceneNumber(int sceneNumber) {
        this.sceneNumber = sceneNumber;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
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
}