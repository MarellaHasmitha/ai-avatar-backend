package com.aiavatar.aibackend.controller;

import com.aiavatar.aibackend.entity.Avatar;
import com.aiavatar.aibackend.service.AvatarService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/avatars")
public class AvatarController {

    private final AvatarService avatarService;

    public AvatarController(AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<AvatarResponse> uploadAvatar(
            @RequestParam("name") String name,
            @RequestParam("avatar") MultipartFile avatar,
            Authentication authentication) {

        String email = authentication.getName();

        Avatar savedAvatar =
                avatarService.uploadAvatar(
                        email,
                        name,
                        avatar);

        return ResponseEntity.ok(
                toResponse(savedAvatar));
    }

    @GetMapping
    public ResponseEntity<List<AvatarResponse>> getMyAvatars(
            Authentication authentication) {

        // We'll implement this after upload testing.
        return ResponseEntity.ok(List.of());
    }

    private AvatarResponse toResponse(Avatar avatar) {

        return new AvatarResponse(
                avatar.getId(),
                avatar.getName(),
                avatar.getContentType(),
                avatar.getCreatedAt());
    }

    public record AvatarResponse(
            Long id,
            String name,
            String contentType,
            LocalDateTime createdAt) {
    }
}