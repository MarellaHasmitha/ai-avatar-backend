package com.aiavatar.aibackend.controller;

import com.aiavatar.aibackend.dto.GenerateVoiceRequest;
import com.aiavatar.aibackend.dto.VideoSceneResponse;
import com.aiavatar.aibackend.entity.VideoScene;
import com.aiavatar.aibackend.service.SceneVoiceService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        "/api/projects/{projectId}/video-generations/{videoGenerationId}/scenes"
)
public class SceneVoiceController {

    private final SceneVoiceService sceneVoiceService;

    public SceneVoiceController(
            SceneVoiceService sceneVoiceService) {

        this.sceneVoiceService = sceneVoiceService;
    }

    @PostMapping("/{sceneId}/voice")
    public ResponseEntity<VideoSceneResponse> generateSceneVoice(
            @PathVariable Long projectId,
            @PathVariable Long videoGenerationId,
            @PathVariable Long sceneId,
            @Valid @RequestBody GenerateVoiceRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        VideoScene scene =
                sceneVoiceService.generateSceneVoice(
                        email,
                        projectId,
                        videoGenerationId,
                        sceneId,
                        request.getVoiceId()
                );

        return ResponseEntity.ok(
                toResponse(scene)
        );
    }

    private VideoSceneResponse toResponse(
            VideoScene scene) {

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
