package com.aiavatar.aibackend.controller;

import com.aiavatar.aibackend.service.FinalVideoMergeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/final-video")
public class FinalVideoMergeController {

    private final FinalVideoMergeService finalVideoMergeService;

    public FinalVideoMergeController(
            FinalVideoMergeService finalVideoMergeService) {

        this.finalVideoMergeService = finalVideoMergeService;
    }

    @PostMapping("/merge/{videoGenerationId}")
    public ResponseEntity<?> mergeFinalVideo(
            @PathVariable Long videoGenerationId) {

        try {

            String finalVideoPath =
                    finalVideoMergeService
                            .mergeFinalVideo(videoGenerationId);

            Map<String, Object> response =
                    new HashMap<>();

            response.put("success", true);
            response.put(
                    "videoGenerationId",
                    videoGenerationId
            );
            response.put(
                    "finalVideoPath",
                    finalVideoPath
            );
            response.put(
                    "message",
                    "Final video merged successfully"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            Map<String, Object> response =
                    new HashMap<>();

            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity
                    .internalServerError()
                    .body(response);
        }
    }
}