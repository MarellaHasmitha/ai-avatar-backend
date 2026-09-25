package com.aiavatar.aibackend.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path audioStoragePath;

    public LocalFileStorageService(
            @Value("${app.storage.audio-path:storage/audio}") String audioPath) {

        this.audioStoragePath = Paths.get(audioPath)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(audioStoragePath);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not create audio storage directory",
                    e
            );
        }
    }

    @Override
    public String saveAudio(
            byte[] audioBytes,
            String fileName) {

        try {

            Path targetPath = audioStoragePath
                    .resolve(fileName)
                    .normalize();

            if (!targetPath.startsWith(audioStoragePath)) {
                throw new RuntimeException(
                        "Invalid audio file name"
                );
            }

            Files.write(targetPath, audioBytes);

            return targetPath.toString();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to save audio file",
                    e
            );
        }
    }
}