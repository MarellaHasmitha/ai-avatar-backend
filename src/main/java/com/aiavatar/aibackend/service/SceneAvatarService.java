package com.aiavatar.aibackend.service;

import com.aiavatar.aibackend.entity.Project;
import com.aiavatar.aibackend.entity.SceneProcessingStatus;
import com.aiavatar.aibackend.entity.User;
import com.aiavatar.aibackend.entity.VideoGeneration;
import com.aiavatar.aibackend.entity.VideoScene;
import com.aiavatar.aibackend.exception.ResourceNotFoundException;
import com.aiavatar.aibackend.repository.ProjectRepository;
import com.aiavatar.aibackend.repository.UserRepository;
import com.aiavatar.aibackend.repository.VideoGenerationRepository;
import com.aiavatar.aibackend.repository.VideoSceneRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SceneAvatarService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final VideoGenerationRepository videoGenerationRepository;
    private final VideoSceneRepository videoSceneRepository;
    private final SyncLabsService syncLabsService;

    public SceneAvatarService(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            VideoGenerationRepository videoGenerationRepository,
            VideoSceneRepository videoSceneRepository,
            SyncLabsService syncLabsService) {

        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.videoGenerationRepository = videoGenerationRepository;
        this.videoSceneRepository = videoSceneRepository;
        this.syncLabsService = syncLabsService;
    }

    @Transactional
    public VideoScene generateAvatarVideo(
            String email,
            Long projectId,
            Long videoGenerationId,
            Long sceneId) {

        // 1. Find authenticated user
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));

        // 2. Verify project ownership
        Project project = projectRepository
                .findByIdAndUser(projectId, user)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found"));

        // 3. Find video generation
        VideoGeneration videoGeneration =
                videoGenerationRepository
                        .findByIdAndProject(
                                videoGenerationId,
                                project)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Video generation not found"));

        // 4. Find scene
        VideoScene scene =
                videoSceneRepository
                        .findByIdAndVideoGeneration(
                                sceneId,
                                videoGeneration)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Video scene not found"));

        // 5. Check whether this scene actually needs an avatar
        if (!scene.isAvatarRequired()) {

            throw new IllegalArgumentException(
                    "Avatar is not required for scene "
                            + scene.getSceneNumber());
        }

        // 6. Check avatar
        if (videoGeneration.getAvatarPath() == null
                || videoGeneration.getAvatarPath().isBlank()) {

            throw new IllegalArgumentException(
                    "No avatar is associated with this video generation");
        }

        // 7. Check scene audio
        if (scene.getAudioUrl() == null
                || scene.getAudioUrl().isBlank()) {

            throw new IllegalArgumentException(
                    "Scene audio has not been generated yet");
        }

        // 8. Mark avatar processing
        scene.setAvatarStatus(
                SceneProcessingStatus.PROCESSING);

        videoSceneRepository.save(scene);

        try {

            String avatarVideoPath =
                    syncLabsService.generateAvatarVideo(
                            videoGeneration.getAvatarPath(),
                            scene.getAudioUrl(),
                            videoGenerationId,
                            scene.getSceneNumber());

            scene.setAvatarVideoUrl(avatarVideoPath);

            scene.setAvatarStatus(
                    SceneProcessingStatus.COMPLETED);

            return videoSceneRepository.save(scene);

        } catch (Exception e) {

            scene.setAvatarStatus(
                    SceneProcessingStatus.FAILED);

            videoSceneRepository.save(scene);

            throw new RuntimeException(
                    "Failed to generate avatar video for scene "
                            + scene.getSceneNumber(),
                    e);
        }
    }
    public VideoScene recoverCompletedAvatarVideo(
            String email,
            Long projectId,
            Long videoGenerationId,
            Long sceneId,
            String syncGenerationId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Project project = projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        VideoGeneration videoGeneration =
                videoGenerationRepository
                        .findByIdAndProject(videoGenerationId, project)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Video generation not found"));

        VideoScene scene =
                videoSceneRepository
                        .findByIdAndVideoGeneration(
                                sceneId, videoGeneration)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Scene not found"));

        if (scene.getAudioUrl() == null
                || scene.getAudioUrl().isBlank()) {
            throw new IllegalArgumentException(
                    "Scene does not have generated audio");
        }

        String avatarVideoPath =
                syncLabsService.downloadCompletedAvatarVideo(
                        syncGenerationId,
                        videoGenerationId,
                        scene.getSceneNumber());

        scene.setAvatarVideoUrl(avatarVideoPath);
        scene.setAvatarStatus(SceneProcessingStatus.COMPLETED);

        return videoSceneRepository.save(scene);
    }
}