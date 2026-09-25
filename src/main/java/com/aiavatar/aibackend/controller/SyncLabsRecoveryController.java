package com.aiavatar.aibackend.controller;

import com.aiavatar.aibackend.service.SyncLabsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sync-recovery")
public class SyncLabsRecoveryController {

    private final SyncLabsService syncLabsService;

    public SyncLabsRecoveryController(
            SyncLabsService syncLabsService) {
        this.syncLabsService = syncLabsService;
    }

    @PostMapping("/download")
    public ResponseEntity<?> downloadExistingVideo(
            @RequestBody Map<String, Object> request) {

        try {

            String outputUrl =
                    (String) request.get("outputUrl");

            Long videoGenerationId =
                    Long.valueOf(
                            request.get("videoGenerationId").toString());

            Integer sceneNumber =
                    Integer.valueOf(
                            request.get("sceneNumber").toString());

            if (outputUrl == null || outputUrl.isBlank()) {
                throw new IllegalArgumentException(
                        "outputUrl is required");
            }

            String savedPath =
                    syncLabsService.downloadAvatarVideoFromOutputUrl(
                            outputUrl,
                            videoGenerationId,
                            sceneNumber);

            Map<String, Object> response =
                    new HashMap<>();

            response.put("success", true);
            response.put("videoGenerationId", videoGenerationId);
            response.put("sceneNumber", sceneNumber);
            response.put("savedPath", savedPath);
            response.put(
                    "message",
                    "Existing Sync Labs video downloaded successfully");

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