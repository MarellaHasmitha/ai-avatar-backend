package com.aiavatar.aibackend.controller;

import com.aiavatar.aibackend.dto.VideoSceneResponse;
import com.aiavatar.aibackend.entity.VideoScene;
import com.aiavatar.aibackend.service.SceneAvatarService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        "/api/projects/{projectId}/video-generations/{videoGenerationId}/scenes"
)
public class SceneAvatarController {

    private final SceneAvatarService sceneAvatarService;

    public SceneAvatarController(
            SceneAvatarService sceneAvatarService) {

        this.sceneAvatarService = sceneAvatarService;
    }

    @PostMapping("/{sceneId}/avatar-video")
    public ResponseEntity<VideoSceneResponse> generateAvatarVideo(
            @PathVariable Long projectId,
            @PathVariable Long videoGenerationId,
            @PathVariable Long sceneId,
            Authentication authentication) {

        String email = authentication.getName();

        VideoScene scene =
                sceneAvatarService.generateAvatarVideo(
                        email,
                        projectId,
                        videoGenerationId,
                        sceneId);

        return ResponseEntity.ok(toResponse(scene));
    }

    private VideoSceneResponse toResponse(VideoScene scene) {

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
    @PostMapping("/{sceneId}/avatar-video/recover")
    public ResponseEntity<VideoSceneResponse> recoverAvatarVideo(
            @PathVariable Long projectId,
            @PathVariable Long videoGenerationId,
            @PathVariable Long sceneId,
            @RequestParam String syncGenerationId,
            Authentication authentication) {

        String email = authentication.getName();

        VideoScene scene =
                sceneAvatarService.recoverCompletedAvatarVideo(
                        email,
                        projectId,
                        videoGenerationId,
                        sceneId,
                        syncGenerationId);

        return ResponseEntity.ok(toResponse(scene));
    }
}