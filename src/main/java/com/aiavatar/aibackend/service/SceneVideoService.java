package com.aiavatar.aibackend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aiavatar.aibackend.entity.Project;
import com.aiavatar.aibackend.entity.User;
import com.aiavatar.aibackend.entity.VideoGeneration;
import com.aiavatar.aibackend.entity.VideoScene;
import com.aiavatar.aibackend.entity.SceneProcessingStatus;
import com.aiavatar.aibackend.repository.ProjectRepository;
import com.aiavatar.aibackend.repository.UserRepository;
import com.aiavatar.aibackend.repository.VideoGenerationRepository;
import com.aiavatar.aibackend.repository.VideoSceneRepository;

@Service
public class SceneVideoService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final VideoGenerationRepository videoGenerationRepository;
    private final VideoSceneRepository videoSceneRepository;
    private final PixazoVideoGenerator pixazoVideoGenerator;

    public SceneVideoService(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            VideoGenerationRepository videoGenerationRepository,
            VideoSceneRepository videoSceneRepository,
            PixazoVideoGenerator pixazoVideoGenerator) {

        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.videoGenerationRepository =
                videoGenerationRepository;
        this.videoSceneRepository =
                videoSceneRepository;
        this.pixazoVideoGenerator =
                pixazoVideoGenerator;
    }

    @Transactional
    public VideoScene generateSceneVideo(
            String email,
            Long projectId,
            Long videoGenerationId,
            Long sceneId) {

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"));

        Project project =
                projectRepository
                        .findByIdAndUser(projectId, user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Project not found"));

        VideoGeneration videoGeneration =
                videoGenerationRepository
                        .findByIdAndProject(
                                videoGenerationId,
                                project)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Video generation not found"));

        VideoScene scene =
                videoSceneRepository
                        .findByIdAndVideoGeneration(
                                sceneId,
                                videoGeneration)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Scene not found"));

        if (scene.isAvatarRequired()) {

            throw new IllegalArgumentException(
                    "This scene requires an avatar. "
                            + "Use the avatar video endpoint instead.");
        }

        if (scene.getVisualPrompt() == null
                || scene.getVisualPrompt().isBlank()) {

            throw new IllegalArgumentException(
                    "Scene does not have a visual prompt");
        }

        scene.setVisualStatus(
                SceneProcessingStatus.PROCESSING);

        scene.setVideoStatus(
                SceneProcessingStatus.PROCESSING);

        videoSceneRepository.save(scene);

        try {

            String videoPath =
            		pixazoVideoGenerator.generateSceneVideo(
                            scene.getVisualPrompt(),
                            scene.getAudioUrl(),
                            videoGenerationId,
                            scene.getSceneNumber());

            scene.setSceneVideoUrl(videoPath);

            scene.setVisualStatus(
                    SceneProcessingStatus.COMPLETED);

            scene.setVideoStatus(
                    SceneProcessingStatus.COMPLETED);

            return videoSceneRepository.save(scene);

        } catch (Exception e) {

            scene.setVisualStatus(
                    SceneProcessingStatus.FAILED);

            scene.setVideoStatus(
                    SceneProcessingStatus.FAILED);

            videoSceneRepository.save(scene);

            throw e;
        }
    }

    @Transactional
    public VideoScene recoverCompletedSceneVideo(
            String email,
            Long projectId,
            Long videoGenerationId,
            Long sceneId,
            String pixazoRequestId) {

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"));

        Project project =
                projectRepository
                        .findByIdAndUser(projectId, user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Project not found"));

        VideoGeneration videoGeneration =
                videoGenerationRepository
                        .findByIdAndProject(
                                videoGenerationId,
                                project)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Video generation not found"));

        VideoScene scene =
                videoSceneRepository
                        .findByIdAndVideoGeneration(
                                sceneId,
                                videoGeneration)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Scene not found"));

        if (scene.isAvatarRequired()) {

            throw new IllegalArgumentException(
                    "This scene requires an avatar. "
                            + "Use the avatar video endpoint instead.");
        }

        String videoPath =
                pixazoVideoGenerator.generateSceneVideo(
                        scene.getVisualPrompt(),
                        scene.getAudioUrl(),
                        videoGenerationId,
                        scene.getSceneNumber());

        scene.setSceneVideoUrl(videoPath);

        scene.setVisualStatus(
                SceneProcessingStatus.COMPLETED);

        scene.setVideoStatus(
                SceneProcessingStatus.COMPLETED);

        return videoSceneRepository.save(scene);
    }
}