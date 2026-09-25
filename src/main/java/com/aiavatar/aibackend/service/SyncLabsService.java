package com.aiavatar.aibackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class SyncLabsService {

    private final RestClient syncRestClient;
    private final Path avatarVideoStoragePath;

    public SyncLabsService(
            @Value("${sync.api-key}") String apiKey,
            @Value("${file.storage.avatar-video-path:storage/avatar-videos}")
            String avatarVideoPath) {

        this.syncRestClient = RestClient.builder()
                .baseUrl("https://api.sync.so")
                .defaultHeader("x-api-key", apiKey)
                .build();

        this.avatarVideoStoragePath =
                Paths.get(avatarVideoPath)
                        .toAbsolutePath()
                        .normalize();

        try {
            Files.createDirectories(avatarVideoStoragePath);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not create avatar video storage directory",
                    e);
        }
    }

    public String generateAvatarVideo(
            String avatarPath,
            String audioPath,
            Long videoGenerationId,
            Integer sceneNumber) {

        try {
            Path avatarFile = Paths.get(avatarPath);
            Path audioFile = Paths.get(audioPath);

            if (!Files.exists(avatarFile)) {
                throw new RuntimeException(
                        "Avatar file not found: " + avatarPath);
            }

            if (!Files.exists(audioFile)) {
                throw new RuntimeException(
                        "Audio file not found: " + audioPath);
            }

            byte[] avatarBytes = Files.readAllBytes(avatarFile);
            byte[] audioBytes = Files.readAllBytes(audioFile);

            System.out.println("========== SYNC LABS ==========");
            System.out.println("Avatar: " + avatarFile);
            System.out.println(
                    "Avatar size: " + avatarBytes.length + " bytes");
            System.out.println("Audio: " + audioFile);
            System.out.println(
                    "Audio size: " + audioBytes.length + " bytes");
            System.out.println(
                    "Starting Sync Labs generation...");
            System.out.println("===============================");

            ByteArrayResource avatarResource =
                    new ByteArrayResource(avatarBytes) {

                        @Override
                        public String getFilename() {
                            return avatarFile
                                    .getFileName()
                                    .toString();
                        }
                    };

            ByteArrayResource audioResource =
                    new ByteArrayResource(audioBytes) {

                        @Override
                        public String getFilename() {
                            return audioFile
                                    .getFileName()
                                    .toString();
                        }
                    };

            MultiValueMap<String, Object> body =
                    new LinkedMultiValueMap<>();

            body.add("image", avatarResource);
            body.add("audio", audioResource);
            body.add("model", "sync-3");

            Map<?, ?> createResponse =
                    syncRestClient.post()
                            .uri("/v2/generate")
                            .contentType(
                                    MediaType.MULTIPART_FORM_DATA)
                            .body(body)
                            .retrieve()
                            .body(Map.class);

            if (createResponse == null) {
                throw new RuntimeException(
                        "Sync Labs returned empty response");
            }

            Object id = createResponse.get("id");

            if (id == null) {
                throw new RuntimeException(
                        "Sync Labs did not return generation ID: "
                                + createResponse);
            }

            String generationId = id.toString();

            System.out.println(
                    "Sync Labs generation created: "
                            + generationId);

            Map<?, ?> result = null;

            int maxAttempts = 60;

            for (int attempt = 1;
                 attempt <= maxAttempts;
                 attempt++) {

                result =
                        syncRestClient.get()
                                .uri(
                                        "/v2/generate/{id}",
                                        generationId)
                                .retrieve()
                                .body(Map.class);

                if (result == null) {
                    throw new RuntimeException(
                            "Sync Labs returned empty generation result");
                }

                Object statusObject =
                        result.get("status");

                String status =
                        statusObject == null
                                ? null
                                : statusObject.toString();

                System.out.println(
                        "Sync Labs generation status: "
                                + status
                                + " (attempt "
                                + attempt
                                + "/"
                                + maxAttempts
                                + ")");

                if ("COMPLETED"
                        .equalsIgnoreCase(status)) {

                    System.out.println(
                            "Sync Labs generation completed successfully.");

                    break;
                }

                if ("FAILED"
                        .equalsIgnoreCase(status)) {

                    throw new RuntimeException(
                            "Sync Labs generation failed: "
                                    + result);
                }

                if ("REJECTED"
                        .equalsIgnoreCase(status)) {

                    throw new RuntimeException(
                            "Sync Labs generation was rejected: "
                                    + result);
                }

                if (attempt == maxAttempts) {

                    throw new RuntimeException(
                            "Sync Labs generation timed out after "
                                    + (maxAttempts * 5)
                                    + " seconds. Generation ID: "
                                    + generationId);
                }

                System.out.println(
                        "Generation still processing. "
                                + "Waiting 5 seconds...");

                Thread.sleep(5000);
            }

            Object outputUrlObject =
                    result.get("outputUrl");

            if (outputUrlObject == null
                    || outputUrlObject.toString().isBlank()) {

                throw new RuntimeException(
                        "Sync Labs completed but returned no outputUrl: "
                                + result);
            }

            String outputUrl =
                    outputUrlObject.toString();

            System.out.println(
                    "Sync Labs output URL received.");

            System.out.println(
                    "Downloading generated avatar video...");

            /*
             * Sync Labs outputUrl can return a redirect.
             *
             * Java HttpClient follows the redirect and
             * downloads the actual MP4.
             */
            byte[] videoBytes =
                    downloadVideoFromUrl(outputUrl);

            saveAvatarVideo(
                    videoBytes,
                    videoGenerationId,
                    sceneNumber);

            Path targetPath =
                    avatarVideoStoragePath.resolve(
                            "video-"
                                    + videoGenerationId
                                    + "-scene-"
                                    + sceneNumber
                                    + "-avatar.mp4")
                            .normalize();

            System.out.println(
                    "Avatar video saved successfully:");

            System.out.println(
                    "File: " + targetPath);

            System.out.println(
                    "Video size: "
                            + Files.size(targetPath)
                            + " bytes");

            System.out.println(
                    "========================================");

            return targetPath.toString();

        } catch (RestClientResponseException e) {

            System.out.println(
                    "========== SYNC LABS ERROR ==========");

            System.out.println(
                    "Status: " + e.getStatusCode());

            System.out.println(
                    "Response: "
                            + e.getResponseBodyAsString());

            System.out.println(
                    "=====================================");

            throw new RuntimeException(
                    "Sync Labs API error: "
                            + e.getResponseBodyAsString(),
                    e);

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to process avatar/video files",
                    e);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Sync Labs generation was interrupted",
                    e);
        }
    }

    public String downloadCompletedAvatarVideo(
            String generationId,
            Long videoGenerationId,
            Integer sceneNumber) {

        try {

            System.out.println(
                    "========== SYNC LABS RECOVERY ==========");

            System.out.println(
                    "Generation ID: " + generationId);

            Map<?, ?> result =
                    syncRestClient.get()
                            .uri(
                                    "/v2/generate/{id}",
                                    generationId)
                            .retrieve()
                            .body(Map.class);

            if (result == null) {
                throw new RuntimeException(
                        "Sync Labs returned empty generation result");
            }

            String status =
                    String.valueOf(
                            result.get("status"));

            System.out.println(
                    "Generation status: " + status);

            if (!"COMPLETED"
                    .equalsIgnoreCase(status)) {

                throw new RuntimeException(
                        "Generation is not completed. "
                                + "Current status: "
                                + status);
            }

            Object outputUrlObject =
                    result.get("outputUrl");

            if (outputUrlObject == null
                    || outputUrlObject.toString().isBlank()) {

                throw new RuntimeException(
                        "Sync Labs completed generation "
                                + "but returned no output URL");
            }

            String outputUrl =
                    outputUrlObject.toString();

            System.out.println(
                    "Output URL received from Sync Labs.");

            System.out.println(
                    "Downloading completed avatar video...");

            byte[] videoBytes =
                    downloadVideoFromUrl(outputUrl);

            saveAvatarVideo(
                    videoBytes,
                    videoGenerationId,
                    sceneNumber);

            Path targetPath =
                    avatarVideoStoragePath.resolve(
                            "video-"
                                    + videoGenerationId
                                    + "-scene-"
                                    + sceneNumber
                                    + "-avatar.mp4")
                            .normalize();

            System.out.println(
                    "Avatar video saved successfully!");

            System.out.println(
                    "File: " + targetPath);

            System.out.println(
                    "Size: "
                            + Files.size(targetPath)
                            + " bytes");

            System.out.println(
                    "========================================");

            return targetPath.toString();

        } catch (RestClientResponseException e) {

            System.out.println(
                    "========== SYNC LABS ERROR ==========");

            System.out.println(
                    "Status: " + e.getStatusCode());

            System.out.println(
                    "Response: "
                            + e.getResponseBodyAsString());

            System.out.println(
                    "=====================================");

            throw new RuntimeException(
                    "Sync Labs API error: "
                            + e.getResponseBodyAsString(),
                    e);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to download completed avatar video",
                    e);
        }
    }

    /*
     * Temporary/manual recovery method.
     *
     * This downloads an already-generated Sync Labs video
     * using its existing output URL.
     *
     * It DOES NOT create a new Sync Labs generation.
     */
    public String downloadAvatarVideoFromOutputUrl(
            String outputUrl,
            Long videoGenerationId,
            Integer sceneNumber) {

        try {

            if (outputUrl == null
                    || outputUrl.isBlank()) {

                throw new IllegalArgumentException(
                        "Sync Labs output URL cannot be empty");
            }

            System.out.println(
                    "========== SYNC LABS MANUAL RECOVERY ==========");

            System.out.println(
                    "Video Generation ID: "
                            + videoGenerationId);

            System.out.println(
                    "Scene Number: "
                            + sceneNumber);

            System.out.println(
                    "Downloading existing Sync Labs video...");

            byte[] videoBytes =
                    downloadVideoFromUrl(outputUrl);

            saveAvatarVideo(
                    videoBytes,
                    videoGenerationId,
                    sceneNumber);

            Path targetPath =
                    avatarVideoStoragePath.resolve(
                            "video-"
                                    + videoGenerationId
                                    + "-scene-"
                                    + sceneNumber
                                    + "-avatar.mp4")
                            .normalize();

            System.out.println(
                    "Existing avatar video saved successfully!");

            System.out.println(
                    "File: " + targetPath);

            System.out.println(
                    "Size: "
                            + Files.size(targetPath)
                            + " bytes");

            System.out.println(
                    "===============================================");

            return targetPath.toString();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to download existing Sync Labs "
                            + "avatar video: "
                            + e.getMessage(),
                    e);
        }
    }

    /*
     * Downloads the actual MP4.
     *
     * Sync Labs may return a 3xx redirect from outputUrl.
     * HttpClient follows the redirect automatically.
     */
    private byte[] downloadVideoFromUrl(
            String outputUrl) {

        try {

            HttpClient httpClient =
                    HttpClient.newBuilder()
                            .followRedirects(
                                    HttpClient.Redirect.NORMAL)
                            .build();

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(outputUrl))
                            .GET()
                            .build();

            HttpResponse<byte[]> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers
                                    .ofByteArray());

            System.out.println(
                    "Video download HTTP status: "
                            + response.statusCode());

            System.out.println(
                    "Video Content-Type: "
                            + response.headers()
                                    .firstValue(
                                            "Content-Type")
                                    .orElse("unknown"));

            System.out.println(
                    "Downloaded bytes: "
                            + response.body().length);

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                throw new RuntimeException(
                        "Video download failed. "
                                + "HTTP status: "
                                + response.statusCode());
            }

            byte[] videoBytes =
                    response.body();

            if (videoBytes == null
                    || videoBytes.length == 0) {

                throw new RuntimeException(
                        "Downloaded video is empty");
            }

            String contentType =
                    response.headers()
                            .firstValue("Content-Type")
                            .orElse("");

            /*
             * Prevent accidentally saving HTML/text
             * as an MP4.
             */
            if (contentType.contains("text/html")
                    || contentType.contains("text/plain")) {

                throw new RuntimeException(
                        "Sync Labs returned text instead of "
                                + "a video. Content-Type: "
                                + contentType);
            }

            return videoBytes;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to download Sync Labs video: "
                            + e.getMessage(),
                    e);
        }
    }

    /*
     * Saves the MP4 to storage/avatar-videos.
     */
    private void saveAvatarVideo(
            byte[] videoBytes,
            Long videoGenerationId,
            Integer sceneNumber)
            throws IOException {

        if (videoBytes == null
                || videoBytes.length == 0) {

            throw new RuntimeException(
                    "Video bytes are empty");
        }

        String fileName =
                "video-"
                        + videoGenerationId
                        + "-scene-"
                        + sceneNumber
                        + "-avatar.mp4";

        Path targetPath =
                avatarVideoStoragePath
                        .resolve(fileName)
                        .normalize();

        if (!targetPath.startsWith(
                avatarVideoStoragePath)) {

            throw new IllegalArgumentException(
                    "Invalid avatar video file path");
        }

        Files.write(
                targetPath,
                videoBytes);

        if (!Files.exists(targetPath)) {

            throw new RuntimeException(
                    "Avatar video file was not created");
        }

        long savedFileSize =
                Files.size(targetPath);

        if (savedFileSize == 0) {

            throw new RuntimeException(
                    "Avatar video file was created "
                            + "but is empty");
        }

        System.out.println(
                "Downloaded video size: "
                        + savedFileSize
                        + " bytes");
    }
}