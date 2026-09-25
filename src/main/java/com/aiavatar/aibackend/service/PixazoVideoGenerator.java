package com.aiavatar.aibackend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.aiavatar.aibackend.dto.VideoGenerationOutputDto;

@Service
public class PixazoVideoGenerator {

    private final RestClient pixazoClient;

    private final String apiKey;

    private final Path sceneVideoStoragePath;

    private static final double FRAME_RATE = 24.0;

    /*
     * Maximum number of times we will tolerate temporary
     * connection problems while polling Pixazo.
     */
    private static final int MAX_CONNECTION_RETRIES = 20;

    public PixazoVideoGenerator(
            @Value("${pixazo.api-key}") String apiKey,
            @Value("${file.storage.scene-video-path}") String storagePath) {

        this.apiKey = apiKey;

        this.pixazoClient = RestClient.builder()
                .baseUrl("https://gateway.pixazo.ai")
                .build();

        this.sceneVideoStoragePath = Paths.get(storagePath)
                .toAbsolutePath()
                .normalize();

        try {

            Files.createDirectories(sceneVideoStoragePath);

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to create scene video storage directory",
                    e);
        }
    }

    // ============================================================
    // Generate new Pixazo scene video
    // ============================================================

    public String generateSceneVideo(
            String visualPrompt,
            String audioPath,
            Long videoGenerationId,
            Integer sceneNumber) {

        try {

            if (visualPrompt == null || visualPrompt.isBlank()) {

                throw new IllegalArgumentException(
                        "Visual prompt cannot be empty");
            }

            if (audioPath == null || audioPath.isBlank()) {

                throw new IllegalArgumentException(
                        "Scene audio path cannot be empty");
            }

            Path audioFile = Paths.get(audioPath)
                    .toAbsolutePath()
                    .normalize();

            if (!Files.exists(audioFile)) {

                throw new IllegalArgumentException(
                        "Scene audio file not found: "
                                + audioFile);
            }

            System.out.println(
                    "========== PIXAZO SCENE VIDEO GENERATION ==========");

            System.out.println(
                    "Generation ID: "
                            + videoGenerationId);

            System.out.println(
                    "Scene Number: "
                            + sceneNumber);

            System.out.println(
                    "Visual Prompt: "
                            + visualPrompt);

            System.out.println(
                    "Audio File: "
                            + audioFile);

            // --------------------------------------------------------
            // STEP 1
            // Measure actual Cartesia audio duration
            // --------------------------------------------------------

            double audioDuration =
                    getAudioDuration(audioFile);

            System.out.println(
                    "Actual Cartesia audio duration: "
                            + audioDuration
                            + " seconds");

            // --------------------------------------------------------
            // STEP 2
            // Calculate Pixazo frame count
            // --------------------------------------------------------

            int numFrames =
                    calculateFrameCount(audioDuration);

            // --------------------------------------------------------
            // STEP 3
            // Calculate resolution
            // --------------------------------------------------------

            int[] resolution =
                    calculateResolution(numFrames);

            int width = resolution[0];

            int height = resolution[1];

            double requestedVideoDuration =
                    numFrames / FRAME_RATE;

            System.out.println(
                    "Pixazo FPS: "
                            + FRAME_RATE);

            System.out.println(
                    "Pixazo frames: "
                            + numFrames);

            System.out.println(
                    "Pixazo resolution: "
                            + width
                            + "x"
                            + height);

            System.out.println(
                    "Requested Pixazo duration: "
                            + requestedVideoDuration
                            + " seconds");

            // --------------------------------------------------------
            // STEP 4
            // Send request to Pixazo
            // --------------------------------------------------------

            Map<String, Object> requestBody =
                    Map.of(

                            "prompt",
                            visualPrompt,

                            "negative",
                            "blurry, low quality, distorted, "
                                    + "duplicate people, distorted hands, "
                                    + "unnatural movement, frozen motion, "
                                    + "text, subtitles, captions, logos, "
                                    + "watermarks",

                            "width",
                            width,

                            "height",
                            height,

                            "num_frames",
                            numFrames,

                            "frame_rate",
                            FRAME_RATE,

                            "steps",
                            8,

                            "cfg",
                            3.0
                    );

            System.out.println(
                    "Sending request to Pixazo...");

            Map<?, ?> response =
                    pixazoClient.post()
                            .uri(
                                    "/ltx-video/v1/text-to-video")
                            .header(
                                    "Ocp-Apim-Subscription-Key",
                                    apiKey)
                            .contentType(
                                    MediaType.APPLICATION_JSON)
                            .body(requestBody)
                            .retrieve()
                            .body(Map.class);

            if (response == null) {

                throw new RuntimeException(
                        "Pixazo returned an empty response");
            }

            System.out.println(
                    "Pixazo generation response: "
                            + response);

            Object requestIdObject =
                    response.get("request_id");

            if (requestIdObject == null) {

                throw new RuntimeException(
                        "Pixazo did not return request_id. "
                                + "Response: "
                                + response);
            }

            String requestId =
                    requestIdObject.toString();

            if (requestId.isBlank()
                    || "null".equalsIgnoreCase(requestId)) {

                throw new RuntimeException(
                        "Pixazo returned an invalid request_id");
            }

            System.out.println(
                    "Pixazo Request ID: "
                            + requestId);

            // --------------------------------------------------------
            // STEP 5
            // Poll Pixazo
            // --------------------------------------------------------

            Path silentVideo =
                    waitForCompletion(
                            requestId,
                            videoGenerationId,
                            sceneNumber);

            // --------------------------------------------------------
            // STEP 6
            // Combine Pixazo video with Cartesia audio
            //
            // NO VIDEO LOOPING
            // --------------------------------------------------------

            return combineVideoWithAudio(
                    silentVideo,
                    audioFile,
                    videoGenerationId,
                    sceneNumber);

        } catch (RestClientResponseException e) {

            System.out.println(
                    "========== PIXAZO ERROR ==========");

            System.out.println(
                    "Status: "
                            + e.getStatusCode());

            System.out.println(
                    "Response: "
                            + e.getResponseBodyAsString());

            System.out.println(
                    "==================================");

            throw new RuntimeException(
                    "Pixazo API error: "
                            + e.getResponseBodyAsString(),
                    e);
        }
    }

    // ============================================================
    // Get actual audio duration using ffprobe
    // ============================================================

    private double getAudioDuration(Path audioFile) {

        try {

            ProcessBuilder processBuilder =
                    new ProcessBuilder(

                            "ffprobe",

                            "-v",
                            "error",

                            "-show_entries",
                            "format=duration",

                            "-of",
                            "default=noprint_wrappers=1:nokey=1",

                            audioFile.toString()
                    );

            processBuilder.redirectErrorStream(true);

            Process process =
                    processBuilder.start();

            String output =
                    new String(
                            process.getInputStream()
                                    .readAllBytes())
                            .trim();

            int exitCode =
                    process.waitFor();

            if (exitCode != 0) {

                throw new RuntimeException(
                        "ffprobe failed while reading audio duration: "
                                + output);
            }

            if (output.isBlank()) {

                throw new RuntimeException(
                        "ffprobe returned empty audio duration");
            }

            double duration =
                    Double.parseDouble(output);

            if (duration <= 0) {

                throw new RuntimeException(
                        "Invalid audio duration: "
                                + duration);
            }

            return duration;

        } catch (IOException e) {

            throw new RuntimeException(
                    "ffprobe is not available. "
                            + "Make sure FFmpeg is installed "
                            + "and added to Windows PATH.",
                    e);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Audio duration detection was interrupted",
                    e);

        } catch (NumberFormatException e) {

            throw new RuntimeException(
                    "Could not parse audio duration",
                    e);
        }
    }

    // ============================================================
    // Get actual video duration using ffprobe
    // ============================================================

    private double getVideoDuration(Path videoFile) {

        try {

            ProcessBuilder processBuilder =
                    new ProcessBuilder(

                            "ffprobe",

                            "-v",
                            "error",

                            "-show_entries",
                            "format=duration",

                            "-of",
                            "default=noprint_wrappers=1:nokey=1",

                            videoFile.toString()
                    );

            processBuilder.redirectErrorStream(true);

            Process process =
                    processBuilder.start();

            String output =
                    new String(
                            process.getInputStream()
                                    .readAllBytes())
                            .trim();

            int exitCode =
                    process.waitFor();

            if (exitCode != 0) {

                throw new RuntimeException(
                        "ffprobe failed while reading video duration: "
                                + output);
            }

            if (output.isBlank()) {

                throw new RuntimeException(
                        "ffprobe returned empty video duration");
            }

            double duration =
                    Double.parseDouble(output);

            if (duration <= 0) {

                throw new RuntimeException(
                        "Invalid video duration: "
                                + duration);
            }

            return duration;

        } catch (IOException e) {

            throw new RuntimeException(
                    "ffprobe is not available. "
                            + "Make sure FFmpeg is installed "
                            + "and added to Windows PATH.",
                    e);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Video duration detection was interrupted",
                    e);

        } catch (NumberFormatException e) {

            throw new RuntimeException(
                    "Could not parse video duration",
                    e);
        }
    }

    // ============================================================
    // Calculate Pixazo frame count
    // ============================================================

    private int calculateFrameCount(
            double audioDuration) {

        int desiredFrames =
                (int) Math.ceil(
                        audioDuration * FRAME_RATE);

        int k =
                Math.max(
                        1,
                        Math.round(
                                (desiredFrames - 1)
                                        / 8.0f));

        int numFrames =
                1 + (8 * k);

        /*
         * Current Pixazo LTX endpoint supports
         * up to 481 frames.
         */

        if (numFrames > 481) {

            throw new IllegalArgumentException(
                    "Audio is too long for the current "
                            + "Pixazo LTX frame limit. "
                            + "Maximum supported duration is "
                            + (481 / FRAME_RATE)
                            + " seconds at "
                            + FRAME_RATE
                            + " FPS.");
        }

        return numFrames;
    }

    // ============================================================
    // Calculate resolution
    // ============================================================

    private int[] calculateResolution(
            int numFrames) {

        /*
         * Conservative resolutions:
         *
         * 896x512
         * 832x480
         * 768x432
         * 704x384
         * 576x320
         */

        if (numFrames <= 217) {

            return new int[] {
                    896,
                    512
            };
        }

        if (numFrames <= 249) {

            return new int[] {
                    832,
                    480
            };
        }

        if (numFrames <= 289) {

            return new int[] {
                    768,
                    432
            };
        }

        if (numFrames <= 361) {

            return new int[] {
                    704,
                    384
            };
        }

        return new int[] {
                576,
                320
        };
    }

    // ============================================================
    // Poll Pixazo until completed
    // ============================================================

    private Path waitForCompletion(
            String requestId,
            Long videoGenerationId,
            Integer sceneNumber) {

        int maxAttempts = 120;

        int pollingIntervalSeconds = 5;

        int connectionRetryCount = 0;

        for (int attempt = 1;
                attempt <= maxAttempts;
                attempt++) {

            try {

                Map<?, ?> result =
                        pixazoClient.get()
                                .uri(
                                        "/v2/requests/status/{requestId}",
                                        requestId)
                                .header(
                                        "Ocp-Apim-Subscription-Key",
                                        apiKey)
                                .retrieve()
                                .body(Map.class);

                connectionRetryCount = 0;

                if (result == null) {

                    throw new RuntimeException(
                            "Pixazo returned empty status response");
                }

                String status =
                        String.valueOf(
                                result.get("status"));

                System.out.println(
                        "Pixazo status ["
                                + attempt
                                + "/"
                                + maxAttempts
                                + "]: "
                                + status);

                // ----------------------------------------------------
                // COMPLETED
                // ----------------------------------------------------

                if ("COMPLETED".equalsIgnoreCase(status)) {

                    System.out.println(
                            "Pixazo generation completed!");

                    String mediaUrl =
                            extractMediaUrl(result);

                    if (mediaUrl == null
                            || mediaUrl.isBlank()) {

                        throw new RuntimeException(
                                "Pixazo completed but returned "
                                        + "no media URL. Response: "
                                        + result);
                    }

                    System.out.println(
                            "Pixazo media URL received.");

                    return downloadVideo(
                            mediaUrl,
                            videoGenerationId,
                            sceneNumber);
                }

                // ----------------------------------------------------
                // FAILED
                // ----------------------------------------------------

                if ("FAILED".equalsIgnoreCase(status)
                        || "ERROR".equalsIgnoreCase(status)) {

                    throw new RuntimeException(
                            "Pixazo video generation failed: "
                                    + result);
                }

                // ----------------------------------------------------
                // PROCESSING / QUEUED
                // ----------------------------------------------------

                System.out.println(
                        "Pixazo job is still running.");

                System.out.println(
                        "Waiting "
                                + pollingIntervalSeconds
                                + " seconds before next poll...");

                Thread.sleep(
                        pollingIntervalSeconds * 1000L);

            } catch (ResourceAccessException e) {

                connectionRetryCount++;

                System.out.println(
                        "================================================");

                System.out.println(
                        "Pixazo polling connection problem.");

                System.out.println(
                        "Request ID: "
                                + requestId);

                System.out.println(
                        "Attempt: "
                                + attempt
                                + "/"
                                + maxAttempts);

                System.out.println(
                        "Connection retry: "
                                + connectionRetryCount
                                + "/"
                                + MAX_CONNECTION_RETRIES);

                System.out.println(
                        "Error: "
                                + e.getMessage());

                System.out.println(
                        "The Pixazo job may still be running.");

                System.out.println(
                        "Will retry status check.");

                System.out.println(
                        "================================================");

                if (connectionRetryCount
                        > MAX_CONNECTION_RETRIES) {

                    throw new RuntimeException(
                            "Pixazo polling failed repeatedly due "
                                    + "to connection problems. "
                                    + "The Pixazo generation may "
                                    + "still be running. "
                                    + "Request ID: "
                                    + requestId,
                            e);
                }

                try {

                    Thread.sleep(
                            pollingIntervalSeconds * 1000L);

                } catch (InterruptedException interruptedException) {

                    Thread.currentThread().interrupt();

                    throw new RuntimeException(
                            "Pixazo polling was interrupted",
                            interruptedException);
                }

                continue;

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                throw new RuntimeException(
                        "Pixazo polling was interrupted",
                        e);

            } catch (RestClientResponseException e) {

                System.out.println(
                        "Pixazo polling API error.");

                System.out.println(
                        "Status: "
                                + e.getStatusCode());

                System.out.println(
                        "Response: "
                                + e.getResponseBodyAsString());

                throw new RuntimeException(
                        "Pixazo polling API error: "
                                + e.getResponseBodyAsString(),
                        e);
            }
        }

        throw new RuntimeException(
                "Pixazo video generation timed out after "
                        + (maxAttempts
                        * pollingIntervalSeconds)
                        + " seconds. "
                        + "Request ID: "
                        + requestId);
    }

    // ============================================================
    // Extract Pixazo media URL
    // ============================================================

    private String extractMediaUrl(
            Map<?, ?> result) {

        Object outputObject =
                result.get("output");

        if (!(outputObject instanceof Map<?, ?> output)) {

            return null;
        }

        Object mediaUrlObject =
                output.get("media_url");

        if (!(mediaUrlObject instanceof List<?> mediaUrls)
                || mediaUrls.isEmpty()) {

            return null;
        }

        Object firstUrl =
                mediaUrls.get(0);

        if (firstUrl == null) {

            return null;
        }

        return firstUrl.toString();
    }

    // ============================================================
    // Download Pixazo visual-only video
    // ============================================================

    private Path downloadVideo(
            String mediaUrl,
            Long videoGenerationId,
            Integer sceneNumber) {

        try {

            System.out.println(
                    "Downloading Pixazo video...");

            byte[] videoBytes =
                    RestClient.create()
                            .get()
                            .uri(mediaUrl)
                            .retrieve()
                            .body(byte[].class);

            if (videoBytes == null
                    || videoBytes.length == 0) {

                throw new RuntimeException(
                        "Downloaded Pixazo video is empty");
            }

            String fileName =
                    "video-"
                            + videoGenerationId
                            + "-scene-"
                            + sceneNumber
                            + "-visual.mp4";

            Path targetPath =
                    sceneVideoStoragePath
                            .resolve(fileName)
                            .normalize();

            if (!targetPath.startsWith(
                    sceneVideoStoragePath)) {

                throw new IllegalArgumentException(
                        "Invalid scene video file path");
            }

            Files.write(
                    targetPath,
                    videoBytes);

            System.out.println(
                    "Pixazo visual video saved:");

            System.out.println(
                    targetPath);

            System.out.println(
                    "Size: "
                            + videoBytes.length
                            + " bytes");

            return targetPath;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to download Pixazo video",
                    e);
        }
    }

    // ============================================================
    // Combine Pixazo video with Cartesia audio
    // ============================================================

    private String combineVideoWithAudio(
            Path videoPath,
            Path audioPath,
            Long videoGenerationId,
            Integer sceneNumber) {

        try {

            String fileName =
                    "video-"
                            + videoGenerationId
                            + "-scene-"
                            + sceneNumber
                            + ".mp4";

            Path finalVideoPath =
                    sceneVideoStoragePath
                            .resolve(fileName)
                            .normalize();

            if (!finalVideoPath.startsWith(
                    sceneVideoStoragePath)) {

                throw new IllegalArgumentException(
                        "Invalid final video file path");
            }

            System.out.println(
                    "================================================");

            System.out.println(
                    "Combining Pixazo video with Cartesia audio...");

            System.out.println(
                    "Pixazo video: "
                            + videoPath);

            System.out.println(
                    "Cartesia audio: "
                            + audioPath);

            System.out.println(
                    "NO VIDEO LOOPING");

            System.out.println(
                    "================================================");

            ProcessBuilder processBuilder =
                    new ProcessBuilder(

                            "ffmpeg",

                            "-y",

                            // Pixazo video
                            "-i",
                            videoPath.toString(),

                            // Cartesia audio
                            "-i",
                            audioPath.toString(),

                            // Video stream
                            "-map",
                            "0:v:0",

                            // Audio stream
                            "-map",
                            "1:a:0",

                            // Video codec
                            "-c:v",
                            "libx264",

                            "-preset",
                            "veryfast",

                            "-crf",
                            "23",

                            "-pix_fmt",
                            "yuv420p",

                            // Audio codec
                            "-c:a",
                            "aac",

                            "-b:a",
                            "192k",

                            // Stop when shorter input ends
                            "-shortest",

                            // Better MP4 playback
                            "-movflags",
                            "+faststart",

                            finalVideoPath.toString()
                    );

            processBuilder.redirectErrorStream(true);

            Process process =
                    processBuilder.start();

            String ffmpegOutput =
                    new String(
                            process.getInputStream()
                                    .readAllBytes());

            int exitCode =
                    process.waitFor();

            System.out.println(
                    "FFmpeg output:");

            System.out.println(
                    ffmpegOutput);

            if (exitCode != 0) {

                throw new RuntimeException(
                        "FFmpeg failed with exit code "
                                + exitCode
                                + "\n"
                                + ffmpegOutput);
            }

            if (!Files.exists(finalVideoPath)
                    || Files.size(finalVideoPath) == 0) {

                throw new RuntimeException(
                        "FFmpeg created an empty video file");
            }

            /*
             * Delete temporary visual-only video.
             */
            Files.deleteIfExists(videoPath);

            System.out.println(
                    "================================================");

            System.out.println(
                    "Final scene video created successfully!");

            System.out.println(
                    "Final video: "
                            + finalVideoPath);

            System.out.println(
                    "Size: "
                            + Files.size(finalVideoPath)
                            + " bytes");

            System.out.println(
                    "================================================");

            return finalVideoPath.toString();

        } catch (IOException e) {

            throw new RuntimeException(
                    "FFmpeg is not available or could not "
                            + "be executed. "
                            + "Install FFmpeg and add it "
                            + "to Windows PATH.",
                    e);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "FFmpeg process was interrupted",
                    e);
        }
    }

    // ============================================================
    // Recover already completed Pixazo generation
    //
    // IMPORTANT:
    // This does NOT create a new Pixazo request.
    //
    // It uses an existing COMPLETED request ID.
    // Then:
    //
    // Pixazo video
    //       +
    // Cartesia audio
    //       ↓
    // FFmpeg
    //       ↓
    // Final scene video
    // ============================================================

    public VideoGenerationOutputDto recoverCompletedSceneVideo(
            String requestId,
            String audioPath,
            Long videoGenerationId,
            Long sceneId,
            Integer sceneNumber) {

        try {

            System.out.println(
                    "========== PIXAZO COMPLETED VIDEO RECOVERY ==========");

            System.out.println(
                    "Request ID: "
                            + requestId);

            System.out.println(
                    "Video Generation ID: "
                            + videoGenerationId);

            System.out.println(
                    "Scene ID: "
                            + sceneId);

            System.out.println(
                    "Scene Number: "
                            + sceneNumber);

            // --------------------------------------------------------
            // STEP 1
            // Validate audio
            // --------------------------------------------------------

            if (audioPath == null
                    || audioPath.isBlank()) {

                throw new IllegalArgumentException(
                        "Scene audio path cannot be empty");
            }

            Path audioFile =
                    Paths.get(audioPath)
                            .toAbsolutePath()
                            .normalize();

            if (!Files.exists(audioFile)) {

                throw new IllegalArgumentException(
                        "Scene audio file not found: "
                                + audioFile);
            }

            // --------------------------------------------------------
            // STEP 2
            // Get actual Cartesia audio duration
            // --------------------------------------------------------

            double audioDuration =
                    getAudioDuration(audioFile);

            System.out.println(
                    "Actual Cartesia audio duration: "
                            + audioDuration
                            + " seconds");

            // --------------------------------------------------------
            // STEP 3
            // Check existing Pixazo request
            // --------------------------------------------------------

            Map<?, ?> result =
                    pixazoClient.get()
                            .uri(
                                    "/v2/requests/status/{requestId}",
                                    requestId)
                            .header(
                                    "Ocp-Apim-Subscription-Key",
                                    apiKey)
                            .retrieve()
                            .body(Map.class);

            if (result == null) {

                throw new RuntimeException(
                        "Pixazo returned an empty status response");
            }

            String status =
                    String.valueOf(
                            result.get("status"));

            System.out.println(
                    "Pixazo status: "
                            + status);

            // --------------------------------------------------------
            // STEP 4
            // Ensure completed
            // --------------------------------------------------------

            if ("FAILED".equalsIgnoreCase(status)
                    || "ERROR".equalsIgnoreCase(status)) {

                throw new RuntimeException(
                        "Pixazo video generation failed: "
                                + result);
            }

            if (!"COMPLETED".equalsIgnoreCase(status)) {

                throw new RuntimeException(
                        "Pixazo video is not completed yet. "
                                + "Current status: "
                                + status);
            }

            // --------------------------------------------------------
            // STEP 5
            // Extract media URL
            // --------------------------------------------------------

            String mediaUrl =
                    extractMediaUrl(result);

            if (mediaUrl == null
                    || mediaUrl.isBlank()) {

                throw new RuntimeException(
                        "Pixazo completed but returned no media URL. "
                                + "Response: "
                                + result);
            }

            System.out.println(
                    "Pixazo media URL received.");

            // --------------------------------------------------------
            // STEP 6
            // Download visual-only video
            // --------------------------------------------------------

            Path visualVideoPath =
                    downloadVideo(
                            mediaUrl,
                            videoGenerationId,
                            sceneNumber);

            // --------------------------------------------------------
            // STEP 7
            // Measure actual Pixazo video duration
            //
            // Do this BEFORE FFmpeg because FFmpeg deletes
            // the temporary visual-only file.
            // --------------------------------------------------------

            double pixazoDuration =
                    getVideoDuration(
                            visualVideoPath);

            System.out.println(
                    "Actual Pixazo video duration: "
                            + pixazoDuration
                            + " seconds");

            // --------------------------------------------------------
            // STEP 8
            // Combine Pixazo video + Cartesia audio
            // --------------------------------------------------------

            String finalVideoPath =
                    combineVideoWithAudio(
                            visualVideoPath,
                            audioFile,
                            videoGenerationId,
                            sceneNumber);

            // --------------------------------------------------------
            // STEP 9
            // Build output DTO
            // --------------------------------------------------------

            VideoGenerationOutputDto response =
                    new VideoGenerationOutputDto(

                            videoGenerationId,

                            sceneId,

                            sceneNumber,

                            "COMPLETED",

                            audioDuration,

                            pixazoDuration,

                            audioFile.toString(),

                            visualVideoPath.toString(),

                            finalVideoPath,

                            true,

                            requestId,

                            "Scene video recovered successfully "
                                    + "with Cartesia audio."
                    );

            System.out.println(
                    "================================================");

            System.out.println(
                    "Scene video recovery completed.");

            System.out.println(
                    "Final video: "
                            + finalVideoPath);

            System.out.println(
                    "Audio included: true");

            System.out.println(
                    "================================================");

            return response;

        } catch (ResourceAccessException e) {

            throw new RuntimeException(
                    "Pixazo recovery connection error. "
                            + "The generation may still exist on "
                            + "Pixazo. Request ID: "
                            + requestId,
                    e);

        } catch (RestClientResponseException e) {

            throw new RuntimeException(
                    "Pixazo recovery API error: "
                            + e.getResponseBodyAsString(),
                    e);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to recover completed Pixazo video",
                    e);
        }
    }

    // ============================================================
    // Legacy recovery method
    //
    // Kept so any existing code using this method does not break.
    //
    // NOTE:
    // This downloads ONLY the visual Pixazo video.
    // New code should use recoverCompletedSceneVideo().
    // ============================================================

    public String downloadCompletedSceneVideo(
            String requestId,
            Long videoGenerationId,
            Integer sceneNumber) {

        try {

            System.out.println(
                    "========== PIXAZO RECOVERY ==========");

            System.out.println(
                    "Request ID: "
                            + requestId);

            Map<?, ?> result =
                    pixazoClient.get()
                            .uri(
                                    "/v2/requests/status/{requestId}",
                                    requestId)
                            .header(
                                    "Ocp-Apim-Subscription-Key",
                                    apiKey)
                            .retrieve()
                            .body(Map.class);

            if (result == null) {

                throw new RuntimeException(
                        "Pixazo returned empty response");
            }

            String status =
                    String.valueOf(
                            result.get("status"));

            System.out.println(
                    "Pixazo status: "
                            + status);

            if (!"COMPLETED".equalsIgnoreCase(status)) {

                throw new RuntimeException(
                        "Pixazo generation is not completed. "
                                + "Current status: "
                                + status);
            }

            String mediaUrl =
                    extractMediaUrl(result);

            if (mediaUrl == null
                    || mediaUrl.isBlank()) {

                throw new RuntimeException(
                        "Pixazo completed but returned "
                                + "no media URL");
            }

            Path savedPath =
                    downloadVideo(
                            mediaUrl,
                            videoGenerationId,
                            sceneNumber);

            System.out.println(
                    "Pixazo recovery completed!");

            System.out.println(
                    "====================================");

            return savedPath.toString();

        } catch (ResourceAccessException e) {

            throw new RuntimeException(
                    "Pixazo recovery connection error. "
                            + "The generation may still exist on "
                            + "Pixazo. Request ID: "
                            + requestId,
                    e);

        } catch (RestClientResponseException e) {

            throw new RuntimeException(
                    "Pixazo recovery API error: "
                            + e.getResponseBodyAsString(),
                    e);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to recover completed Pixazo video",
                    e);
        }
    }
}