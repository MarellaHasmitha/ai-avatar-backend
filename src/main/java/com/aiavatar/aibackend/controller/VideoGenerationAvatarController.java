package com.aiavatar.aibackend.controller;

import com.aiavatar.aibackend.entity.Avatar;
import com.aiavatar.aibackend.entity.VideoGeneration;
import com.aiavatar.aibackend.repository.AvatarRepository;
import com.aiavatar.aibackend.repository.ProjectRepository;
import com.aiavatar.aibackend.repository.UserRepository;
import com.aiavatar.aibackend.repository.VideoGenerationRepository;
import com.aiavatar.aibackend.entity.User;
import com.aiavatar.aibackend.entity.Project;
import com.aiavatar.aibackend.exception.ResourceNotFoundException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/video-generations/{videoGenerationId}")
public class VideoGenerationAvatarController {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final VideoGenerationRepository videoGenerationRepository;
    private final AvatarRepository avatarRepository;

    public VideoGenerationAvatarController(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            VideoGenerationRepository videoGenerationRepository,
            AvatarRepository avatarRepository) {

        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.videoGenerationRepository = videoGenerationRepository;
        this.avatarRepository = avatarRepository;
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> assignAvatar(
            @PathVariable Long projectId,
            @PathVariable Long videoGenerationId,
            @RequestBody AssignAvatarRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Project project = projectRepository
                .findByIdAndUser(projectId, user)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found"));

        VideoGeneration videoGeneration =
                videoGenerationRepository
                        .findByIdAndProject(videoGenerationId, project)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Video generation not found"));

        Avatar avatar =
                avatarRepository
                        .findByIdAndUser(request.avatarId(), user)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Avatar not found"));

        videoGeneration.setAvatarPath(avatar.getFilePath());

        videoGenerationRepository.save(videoGeneration);

        return ResponseEntity.ok(
                new AssignAvatarResponse(
                        videoGeneration.getId(),
                        avatar.getId(),
                        avatar.getName(),
                        avatar.getFilePath()
                )
        );
    }

    public record AssignAvatarRequest(Long avatarId) {
    }

    public record AssignAvatarResponse(
            Long videoGenerationId,
            Long avatarId,
            String avatarName,
            String avatarPath
    ) {
    }
}