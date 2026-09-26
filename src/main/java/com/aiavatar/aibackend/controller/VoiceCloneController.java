package com.aiavatar.aibackend.controller;

import com.aiavatar.aibackend.service.ElevenLabsVoiceCloningService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/voice")
@ConditionalOnProperty(
        name = "tts.provider",
        havingValue = "elevenlabs"
)
public class VoiceCloneController {

    private final ElevenLabsVoiceCloningService elevenLabsVoiceCloningService;

    public VoiceCloneController(
            ElevenLabsVoiceCloningService elevenLabsVoiceCloningService) {

        this.elevenLabsVoiceCloningService =
                elevenLabsVoiceCloningService;
    }

    // KEEP YOUR EXISTING ENDPOINT METHODS HERE
}