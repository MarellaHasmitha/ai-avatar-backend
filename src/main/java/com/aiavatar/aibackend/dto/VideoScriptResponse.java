package com.aiavatar.aibackend.dto;

import java.util.List;

public class VideoScriptResponse {

    private String title;
    private int totalDuration;
    private List<SceneResponse> scenes;

    public VideoScriptResponse() {
    }

    public VideoScriptResponse(
            String title,
            int totalDuration,
            List<SceneResponse> scenes) {
        this.title = title;
        this.totalDuration = totalDuration;
        this.scenes = scenes;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(int totalDuration) {
        this.totalDuration = totalDuration;
    }

    public List<SceneResponse> getScenes() {
        return scenes;
    }

    public void setScenes(List<SceneResponse> scenes) {
        this.scenes = scenes;
    }
}