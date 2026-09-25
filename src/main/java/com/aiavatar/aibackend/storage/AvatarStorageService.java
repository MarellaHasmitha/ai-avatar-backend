package com.aiavatar.aibackend.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class AvatarStorageService {

    private final Path avatarStoragePath;

    public AvatarStorageService(
            @Value("${file.storage.avatar-path:storage/avatars}") String avatarPath) {

        this.avatarStoragePath = Paths.get(avatarPath)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(avatarStoragePath);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not create avatar storage directory",
                    e
            );
        }
    }

    public String saveAvatar(
            MultipartFile avatarFile,
            Long videoGenerationId) {

        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new IllegalArgumentException(
                    "Avatar file cannot be empty"
            );
        }

        String contentType = avatarFile.getContentType();

        if (!"image/png".equalsIgnoreCase(contentType)
                && !"image/jpeg".equalsIgnoreCase(contentType)) {

            throw new IllegalArgumentException(
                    "Avatar must be a PNG or JPEG image"
            );
        }

        String originalName = avatarFile.getOriginalFilename();

        String extension = ".png";

        if (originalName != null
                && originalName.toLowerCase().endsWith(".jpg")) {
            extension = ".jpg";
        } else if (originalName != null
                && originalName.toLowerCase().endsWith(".jpeg")) {
            extension = ".jpeg";
        }

        String fileName =
                "video_" + videoGenerationId
                        + "_avatar_"
                        + UUID.randomUUID()
                        + extension;

        Path targetPath = avatarStoragePath
                .resolve(fileName)
                .normalize();

        if (!targetPath.startsWith(avatarStoragePath)) {
            throw new IllegalArgumentException(
                    "Invalid avatar file name"
            );
        }

        try {
            Files.write(
                    targetPath,
                    avatarFile.getBytes()
            );

            return targetPath.toString();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to save avatar file",
                    e
            );
        }
    }
}