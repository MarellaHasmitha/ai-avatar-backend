package com.aiavatar.aibackend.service;

import com.aiavatar.aibackend.entity.VideoGeneration;
import com.aiavatar.aibackend.entity.VideoScene;
import com.aiavatar.aibackend.repository.VideoGenerationRepository;
import com.aiavatar.aibackend.repository.VideoSceneRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class FinalVideoMergeService {

    private final VideoSceneRepository videoSceneRepository;
    private final VideoGenerationRepository videoGenerationRepository;

    public FinalVideoMergeService(
            VideoSceneRepository videoSceneRepository,
            VideoGenerationRepository videoGenerationRepository) {

        this.videoSceneRepository = videoSceneRepository;
        this.videoGenerationRepository = videoGenerationRepository;
    }

    public String mergeFinalVideo(Long videoGenerationId)
            throws IOException, InterruptedException {

        // ============================================================
        // 1. Find VideoGeneration
        // ============================================================

        VideoGeneration videoGeneration =
                videoGenerationRepository.findById(videoGenerationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Video generation not found: "
                                                + videoGenerationId
                                )
                        );

        // ============================================================
        // 2. Get all scenes in scene-number order
        // ============================================================

        List<VideoScene> scenes =
                videoSceneRepository
                        .findByVideoGenerationOrderBySceneNumberAsc(
                                videoGeneration
                        );

        if (scenes == null || scenes.isEmpty()) {

            throw new RuntimeException(
                    "No scenes found for video generation: "
                            + videoGenerationId
            );
        }

        // ============================================================
        // 3. Validate every scene has a video
        // ============================================================

        for (VideoScene scene : scenes) {

            String videoPath = getVideoPath(scene);

            if (videoPath == null || videoPath.isBlank()) {

                throw new RuntimeException(
                        "Scene "
                                + scene.getSceneNumber()
                                + " does not have a completed video."
                );
            }

            Path sceneVideo = Paths.get(videoPath);

            if (!Files.exists(sceneVideo)) {

                throw new RuntimeException(
                        "Scene video file not found for scene "
                                + scene.getSceneNumber()
                                + ": "
                                + sceneVideo
                );
            }
        }

        // ============================================================
        // 4. Create final-videos directory
        // ============================================================

        Path finalDirectory =
                Paths.get("storage", "final-videos");

        Files.createDirectories(finalDirectory);

        // ============================================================
        // 5. Final video path
        // ============================================================

        Path finalVideoPath =
                finalDirectory.resolve(
                        "video-"
                                + videoGenerationId
                                + "-final.mp4"
                );

        // ============================================================
        // 6. Temporary FFmpeg concat file
        // ============================================================

        Path concatFile =
                finalDirectory.resolve(
                        "video-"
                                + videoGenerationId
                                + "-concat.txt"
                );

        StringBuilder concatContent =
                new StringBuilder();

        for (VideoScene scene : scenes) {

            String videoPath = getVideoPath(scene);

            Path sceneVideo =
                    Paths.get(videoPath);

            String absolutePath =
                    sceneVideo
                            .toAbsolutePath()
                            .toString()
                            .replace("\\", "/");

            concatContent
                    .append("file '")
                    .append(absolutePath)
                    .append("'")
                    .append(System.lineSeparator());
        }

        Files.writeString(
                concatFile,
                concatContent.toString()
        );

        // ============================================================
        // 7. Run FFmpeg
        // ============================================================

        ProcessBuilder processBuilder =
                new ProcessBuilder(

                        "ffmpeg",

                        "-y",

                        "-f",
                        "concat",

                        "-safe",
                        "0",

                        "-i",
                        concatFile
                                .toAbsolutePath()
                                .toString(),

                        // Re-encode for compatibility
                        "-c:v",
                        "libx264",

                        "-preset",
                        "veryfast",

                        "-crf",
                        "23",

                        "-c:a",
                        "aac",

                        "-b:a",
                        "192k",

                        "-movflags",
                        "+faststart",

                        finalVideoPath
                                .toAbsolutePath()
                                .toString()
                );

        processBuilder.redirectErrorStream(true);

        Process process =
                processBuilder.start();

        String ffmpegOutput =
                new String(
                        process.getInputStream()
                                .readAllBytes()
                );

        int exitCode =
                process.waitFor();

        // ============================================================
        // 8. Check FFmpeg result
        // ============================================================

        if (exitCode != 0) {

            Files.deleteIfExists(concatFile);

            throw new RuntimeException(
                    "FFmpeg final merge failed.\n"
                            + ffmpegOutput
            );
        }

        // ============================================================
        // 9. Check final video
        // ============================================================

        if (!Files.exists(finalVideoPath)
                || Files.size(finalVideoPath) == 0) {

            Files.deleteIfExists(concatFile);

            throw new RuntimeException(
                    "Final video was not created."
            );
        }

        // ============================================================
        // 10. Delete temporary concat file
        // ============================================================

        Files.deleteIfExists(concatFile);

        System.out.println(
                "Final video created successfully: "
                        + finalVideoPath.toAbsolutePath()
        );

        return finalVideoPath
                .toAbsolutePath()
                .toString();
    }

    // ================================================================
    // Select correct video for each scene
    // ================================================================

    private String getVideoPath(VideoScene scene) {

        /*
         * If the scene has an avatar video,
         * use the avatar video.
         *
         * Otherwise use the normal scene video.
         */

        if (scene.getAvatarVideoUrl() != null
                && !scene.getAvatarVideoUrl().isBlank()) {

            return scene.getAvatarVideoUrl();
        }

        if (scene.getSceneVideoUrl() != null
                && !scene.getSceneVideoUrl().isBlank()) {

            return scene.getSceneVideoUrl();
        }

        return null;
    }
}