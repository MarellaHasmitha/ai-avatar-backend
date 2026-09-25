package com.aiavatar.aibackend.controller;

import com.aiavatar.aibackend.dto.VideoGenerationResponse;
import com.aiavatar.aibackend.dto.VideoSceneResponse;
import com.aiavatar.aibackend.entity.VideoGeneration;
import com.aiavatar.aibackend.entity.VideoScene;
import com.aiavatar.aibackend.service.VideoGenerationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/video-generations")
public class VideoGenerationController {

    private final VideoGenerationService videoGenerationService;

    public VideoGenerationController(
            VideoGenerationService videoGenerationService) {

        this.videoGenerationService = videoGenerationService;
    }

    // ============================================================
    // Generate a new video
    //
    // Request:
    // multipart/form-data
    //
    // prompt = text
    // avatar = PNG/JPG/JPEG
    // ============================================================

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<VideoGenerationResponse> generateVideo(
            @PathVariable Long projectId,
            @RequestParam("prompt") String prompt,
            @RequestParam("avatar") MultipartFile avatar,
            Authentication authentication) {

        String email = authentication.getName();

        VideoGeneration videoGeneration =
                videoGenerationService.generateVideo(
                        email,
                        projectId,
                        prompt,
                        avatar
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(videoGeneration));
    }

    // ============================================================
    // Get all video generations for a project
    // ============================================================

    @GetMapping
    public ResponseEntity<List<VideoGenerationResponse>>
    getVideoGenerations(
            @PathVariable Long projectId,
            Authentication authentication) {

        String email = authentication.getName();

        List<VideoGenerationResponse> responses =
                videoGenerationService
                        .getVideoGenerations(
                                email,
                                projectId
                        )
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(responses);
    }

    // ============================================================
    // Get one video generation
    // ============================================================

    @GetMapping("/{videoGenerationId}")
    public ResponseEntity<VideoGenerationResponse>
    getVideoGeneration(
            @PathVariable Long projectId,
            @PathVariable Long videoGenerationId,
            Authentication authentication) {

        String email = authentication.getName();

        VideoGeneration videoGeneration =
                videoGenerationService
                        .getVideoGeneration(
                                email,
                                projectId,
                                videoGenerationId
                        );

        return ResponseEntity.ok(
                toResponse(videoGeneration)
        );
    }

    // ============================================================
    // Get all scenes for a video generation
    // ============================================================

    @GetMapping("/{videoGenerationId}/scenes")
    public ResponseEntity<List<VideoSceneResponse>>
    getScenes(
            @PathVariable Long projectId,
            @PathVariable Long videoGenerationId,
            Authentication authentication) {

        String email = authentication.getName();

        List<VideoSceneResponse> scenes =
                videoGenerationService
                        .getScenes(
                                email,
                                projectId,
                                videoGenerationId
                        )
                        .stream()
                        .map(this::toSceneResponse)
                        .toList();

        return ResponseEntity.ok(scenes);
    }

    // ============================================================
    // Convert VideoGeneration entity to DTO
    // ============================================================

    private VideoGenerationResponse toResponse(
            VideoGeneration videoGeneration) {

        return new VideoGenerationResponse(
                videoGeneration.getId(),
                videoGeneration.getProject().getId(),
                videoGeneration.getPrompt(),
                videoGeneration.getStatus(),
                videoGeneration.getCreatedAt(),
                videoGeneration.getUpdatedAt()
        );
    }

    // ============================================================
    // Convert VideoScene entity to DTO
    // ============================================================

    private VideoSceneResponse toSceneResponse(
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