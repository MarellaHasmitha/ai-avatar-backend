package com.aiavatar.aibackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.aiavatar.aibackend.dto.VideoSceneResponse;
import com.aiavatar.aibackend.entity.VideoScene;
import com.aiavatar.aibackend.service.SceneVideoService;

@RestController
@RequestMapping("/api/projects/{projectId}/video-generations/{videoGenerationId}/scenes")
public class SceneVideoController {

    private final SceneVideoService sceneVideoService;

    public SceneVideoController(SceneVideoService sceneVideoService) {
        this.sceneVideoService = sceneVideoService;
    }

    @PostMapping("/{sceneId}/video")
    public ResponseEntity<VideoSceneResponse> generateSceneVideo(
            @PathVariable Long projectId,
            @PathVariable Long videoGenerationId,
            @PathVariable Long sceneId,
            Authentication authentication) {

        String email = authentication.getName();

        VideoScene scene =
                sceneVideoService.generateSceneVideo(
                        email,
                        projectId,
                        videoGenerationId,
                        sceneId);

        return ResponseEntity.ok(toSceneResponse(scene));
    }

    @PostMapping("/{sceneId}/video/recover")
    public ResponseEntity<VideoSceneResponse> recoverSceneVideo(
            @PathVariable Long projectId,
            @PathVariable Long videoGenerationId,
            @PathVariable Long sceneId,
            @RequestParam String pixazoRequestId,
            Authentication authentication) {

        String email = authentication.getName();

        VideoScene scene =
                sceneVideoService.recoverCompletedSceneVideo(
                        email,
                        projectId,
                        videoGenerationId,
                        sceneId,
                        pixazoRequestId);

        return ResponseEntity.ok(toSceneResponse(scene));
    }

    private VideoSceneResponse toSceneResponse(VideoScene scene) {

        return new VideoSceneResponse(
                scene.getId(),
                scene.getSceneNumber(),
                scene.getDuration(),
                scene.getNarration(),
                scene.getVisualPrompt(),
                scene.isAvatarRequired(),
                scene.getVoiceStatus(),
                scene.getAudioUrl(),
                scene.getVisualStatus(),
                scene.getVisualUrl(),
                scene.getAvatarStatus(),
                scene.getAvatarVideoUrl(),
                scene.getVideoStatus(),
                scene.getSceneVideoUrl()
        );
    }
}