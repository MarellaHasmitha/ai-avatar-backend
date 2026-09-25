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
import com.aiavatar.aibackend.storage.FileStorageService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SceneVoiceService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final VideoGenerationRepository videoGenerationRepository;
    private final VideoSceneRepository videoSceneRepository;
    private final VoiceGenerator voiceGenerator;
    private final FileStorageService fileStorageService;

    public SceneVoiceService(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            VideoGenerationRepository videoGenerationRepository,
            VideoSceneRepository videoSceneRepository,
            VoiceGenerator voiceGenerator,
            FileStorageService fileStorageService) {

        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.videoGenerationRepository = videoGenerationRepository;
        this.videoSceneRepository = videoSceneRepository;
        this.voiceGenerator = voiceGenerator;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public VideoScene generateSceneVoice(
            String email,
            Long projectId,
            Long videoGenerationId,
            Long sceneId,
            String voiceId) {

        // 1. Find the authenticated user
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        // 2. Make sure the project belongs to the user
        Project project = projectRepository
                .findByIdAndUser(projectId, user)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found"
                        ));

        // 3. Find the video generation inside that project
        VideoGeneration videoGeneration =
                videoGenerationRepository
                        .findByIdAndProject(
                                videoGenerationId,
                                project
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Video generation not found"
                                ));

        // 4. Find the scene inside that video generation
        VideoScene scene =
                videoSceneRepository
                        .findByIdAndVideoGeneration(
                                sceneId,
                                videoGeneration
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Video scene not found"
                                ));

        // 5. Mark the scene as processing
        scene.setVoiceStatus(
                SceneProcessingStatus.PROCESSING
        );

        videoSceneRepository.save(scene);

        try {

        // 6. Generate narration audio using the selected TTS provider
            byte[] audioBytes =
                    voiceGenerator.generateVoice(
                            scene.getNarration(),
                            voiceId
                    );

            // 7. Create a unique file name
            String fileName =
                    "video-" + videoGenerationId
                    + "-scene-" + scene.getSceneNumber()
                    + ".mp3";

            // 8. Save the audio file
            String audioPath =
                    fileStorageService.saveAudio(
                            audioBytes,
                            fileName
                    );

            // 9. Save the file location in database
            scene.setAudioUrl(audioPath);

            // 10. Mark voice processing as completed
            scene.setVoiceStatus(
                    SceneProcessingStatus.COMPLETED
            );

            return videoSceneRepository.save(scene);

        } catch (Exception e) {

            // If Cartesia or storage fails
            scene.setVoiceStatus(
                    SceneProcessingStatus.FAILED
            );

            videoSceneRepository.save(scene);

            throw new RuntimeException(
                    "Failed to generate voice for scene "
                    + sceneId,
                    e
            );
        }
    }
}