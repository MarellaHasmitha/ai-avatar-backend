package com.aiavatar.aibackend.service;

import com.aiavatar.aibackend.dto.SceneResponse;
import com.aiavatar.aibackend.dto.VideoScriptResponse;
import com.aiavatar.aibackend.entity.Project;
import com.aiavatar.aibackend.entity.SceneProcessingStatus;
import com.aiavatar.aibackend.entity.User;
import com.aiavatar.aibackend.entity.VideoGeneration;
import com.aiavatar.aibackend.entity.VideoGenerationStatus;
import com.aiavatar.aibackend.entity.VideoScene;
import com.aiavatar.aibackend.exception.ResourceNotFoundException;
import com.aiavatar.aibackend.repository.ProjectRepository;
import com.aiavatar.aibackend.repository.UserRepository;
import com.aiavatar.aibackend.repository.VideoGenerationRepository;
import com.aiavatar.aibackend.repository.VideoSceneRepository;
import com.aiavatar.aibackend.storage.AvatarStorageService;
import com.aiavatar.aibackend.dto.VideoGenerationOutputDto;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class VideoGenerationService {

    private final UserRepository userRepository;

    private final ProjectRepository projectRepository;

    private final VideoGenerationRepository videoGenerationRepository;
   
    private final PixazoVideoGenerator pixazoVideoGenerator;
    
    private final VideoSceneRepository videoSceneRepository;

    private final ScriptGenerator scriptGenerator;

    private final AvatarStorageService avatarStorageService;

    public VideoGenerationService(

            UserRepository userRepository,

            ProjectRepository projectRepository,

            VideoGenerationRepository videoGenerationRepository,

            VideoSceneRepository videoSceneRepository,

            ScriptGenerator scriptGenerator,

            AvatarStorageService avatarStorageService,

            PixazoVideoGenerator pixazoVideoGenerator)  {

        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.videoGenerationRepository = videoGenerationRepository;
        this.videoSceneRepository = videoSceneRepository;
        this.scriptGenerator = scriptGenerator;
        this.avatarStorageService = avatarStorageService;
        this.pixazoVideoGenerator = pixazoVideoGenerator;
    }

    // ============================================================
    // Generate a new video
    // Prompt + Avatar
    // ============================================================

    public VideoGeneration generateVideo(
            String email,
            Long projectId,
            String prompt,
            MultipartFile avatarFile) {

        // --------------------------------------------------------
        // Step 1: Find project belonging to logged-in user
        // --------------------------------------------------------

        Project project = findProjectForUser(
                projectId,
                email
        );

        // --------------------------------------------------------
        // Step 2: Validate basic input
        // --------------------------------------------------------

        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException(
                    "Prompt cannot be empty"
            );
        }

        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new IllegalArgumentException(
                    "Avatar file cannot be empty"
            );
        }

        // --------------------------------------------------------
        // Step 3:
        // Ask OpenRouter to create structured video plan
        // --------------------------------------------------------

        VideoScriptResponse videoScript =
                scriptGenerator.generateVideoScript(prompt);

        // --------------------------------------------------------
        // Step 4:
        // Create parent VideoGeneration record
        // --------------------------------------------------------

        VideoGeneration videoGeneration =
                new VideoGeneration();

        videoGeneration.setProject(project);

        videoGeneration.setPrompt(prompt);

        videoGeneration.setStatus(
                VideoGenerationStatus.SCRIPT_GENERATED
        );

        videoGeneration =
                videoGenerationRepository.save(videoGeneration);

        // --------------------------------------------------------
        // Step 5:
        // Save avatar image
        // --------------------------------------------------------

        String avatarPath =
                avatarStorageService.saveAvatar(
                        avatarFile,
                        videoGeneration.getId()
                );

        videoGeneration.setAvatarPath(avatarPath);

        videoGeneration =
                videoGenerationRepository.save(videoGeneration);

        // --------------------------------------------------------
        // Step 6:
        // Convert every AI-generated scene into DB record
        // --------------------------------------------------------

        for (SceneResponse sceneResponse :
                videoScript.getScenes()) {

            VideoScene scene = new VideoScene();

            scene.setVideoGeneration(
                    videoGeneration
            );

            scene.setSceneNumber(
                    sceneResponse.getSceneNumber()
            );

            scene.setDuration(
                    sceneResponse.getDuration()
            );

            scene.setNarration(
                    sceneResponse.getNarration()
            );

            scene.setVisualPrompt(
                    sceneResponse.getVisualPrompt()
            );

            scene.setAvatarRequired(
                    sceneResponse.isAvatarRequired()
            );

            // Initial processing states

            scene.setVoiceStatus(
                    SceneProcessingStatus.PENDING
            );

            scene.setVisualStatus(
                    SceneProcessingStatus.PENDING
            );

            scene.setAvatarStatus(
                    SceneProcessingStatus.PENDING
            );

            scene.setVideoStatus(
                    SceneProcessingStatus.PENDING
            );

            videoSceneRepository.save(scene);
        }

        // --------------------------------------------------------
        // Step 7:
        // Return created video generation
        // --------------------------------------------------------

        return videoGeneration;
    }

    // ============================================================
    // Get all scenes for a video generation
    // ============================================================

    public List<VideoScene> getScenes(
            String email,
            Long projectId,
            Long videoGenerationId) {

        VideoGeneration videoGeneration =
                getVideoGeneration(
                        email,
                        projectId,
                        videoGenerationId
                );

        return videoSceneRepository
                .findByVideoGenerationOrderBySceneNumberAsc(
                        videoGeneration
                );
    }

    // ============================================================
    // Get one video generation
    // ============================================================

    public VideoGeneration getVideoGeneration(
            String email,
            Long projectId,
            Long videoGenerationId) {

        Project project =
                findProjectForUser(
                        projectId,
                        email
                );

        return videoGenerationRepository
                .findByIdAndProject(
                        videoGenerationId,
                        project
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Video generation not found"
                        )
                );
    }

    // ============================================================
    // Get all video generations for a project
    // ============================================================

    public List<VideoGeneration> getVideoGenerations(
            String email,
            Long projectId) {

        Project project =
                findProjectForUser(
                        projectId,
                        email
                );

        return videoGenerationRepository
                .findByProject(project);
    }

    // ============================================================
    // Find project belonging to logged-in user
    // ============================================================

    private Project findProjectForUser(
            Long projectId,
            String email) {

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        return projectRepository
                .findByIdAndUser(
                        projectId,
                        user
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found"
                        )
                );
    }
}