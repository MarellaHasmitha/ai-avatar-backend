package com.aiavatar.aibackend.service;

import com.aiavatar.aibackend.entity.Avatar;
import com.aiavatar.aibackend.entity.User;
import com.aiavatar.aibackend.exception.ResourceNotFoundException;
import com.aiavatar.aibackend.repository.AvatarRepository;
import com.aiavatar.aibackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class AvatarService {

    private final UserRepository userRepository;
    private final AvatarRepository avatarRepository;
    private final Path avatarStoragePath;

    public AvatarService(
            UserRepository userRepository,
            AvatarRepository avatarRepository,
            @Value("${file.storage.avatar-path:storage/avatars}")
            String avatarPath) {

        this.userRepository = userRepository;
        this.avatarRepository = avatarRepository;

        this.avatarStoragePath = Paths.get(avatarPath)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(avatarStoragePath);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not create avatar storage directory", e);
        }
    }

    public Avatar uploadAvatar(
            String email,
            String name,
            MultipartFile file) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Avatar file cannot be empty");
        }

        String contentType = file.getContentType();

        if (!"image/png".equalsIgnoreCase(contentType)
                && !"image/jpeg".equalsIgnoreCase(contentType)) {

            throw new IllegalArgumentException(
                    "Avatar must be a PNG or JPEG image");
        }

        String originalName = file.getOriginalFilename();

        String extension = ".png";

        if (originalName != null
                && originalName.toLowerCase().endsWith(".jpg")) {

            extension = ".jpg";

        } else if (originalName != null
                && originalName.toLowerCase().endsWith(".jpeg")) {

            extension = ".jpeg";
        }

        String fileName =
                "avatar_"
                + UUID.randomUUID()
                + extension;

        Path targetPath =
                avatarStoragePath
                        .resolve(fileName)
                        .normalize();

        if (!targetPath.startsWith(avatarStoragePath)) {
            throw new IllegalArgumentException(
                    "Invalid avatar file name");
        }

        try {

            Files.write(
                    targetPath,
                    file.getBytes());

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to save avatar file", e);
        }

        Avatar avatar = new Avatar();

        avatar.setUser(user);

        avatar.setName(
                name == null || name.isBlank()
                        ? "My Avatar"
                        : name);

        avatar.setFilePath(
                targetPath.toString());

        avatar.setContentType(contentType);

        return avatarRepository.save(avatar);
    }
}