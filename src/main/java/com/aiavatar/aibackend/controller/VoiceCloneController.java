package com.aiavatar.aibackend.controller;

import com.aiavatar.aibackend.service.ElevenLabsVoiceCloningService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/voices")
public class VoiceCloneController {

    private final ElevenLabsVoiceCloningService voiceCloningService;

    public VoiceCloneController(
            ElevenLabsVoiceCloningService voiceCloningService) {

        this.voiceCloningService = voiceCloningService;
    }

    @PostMapping("/clone")
    public ResponseEntity<Map<String, String>> cloneVoice(
            @RequestParam("name") String voiceName,
            @RequestParam("file") MultipartFile audioFile,
            Authentication authentication) {

        String email = authentication.getName();

        String voiceId =
                voiceCloningService.cloneVoice(
                        voiceName,
                        audioFile
                );

        return ResponseEntity.ok(
                Map.of(
                        "message", "Voice cloned successfully",
                        "user", email,
                        "voiceId", voiceId
                )
        );
    }
}