package com.aiavatar.aibackend.controller;

import com.aiavatar.aibackend.dto.GenerateVoiceRequest;
import com.aiavatar.aibackend.service.VoiceGenerationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class VoiceController {

    private final VoiceGenerationService voiceGenerationService;

    public VoiceController(
            VoiceGenerationService voiceGenerationService) {

        this.voiceGenerationService = voiceGenerationService;
    }

    @PostMapping(
            "/projects/{projectId}/scripts/{scriptId}/voice"
    )
    public ResponseEntity<byte[]> generateVoice(

            @PathVariable Long projectId,

            @PathVariable Long scriptId,

            @Valid @RequestBody GenerateVoiceRequest request,

            Authentication authentication) {

        String email = authentication.getName();

        byte[] audio = voiceGenerationService.generateVoice(
                email,
                projectId,
                scriptId,
                request.getVoiceId()
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"generated-voice.mp3\""
                )
                .contentType(
                        MediaType.parseMediaType("audio/mpeg")
                )
                .body(audio);
    }
}