package com.aiavatar.aibackend.controller;

import com.aiavatar.aibackend.dto.CreateScriptRequest;
import com.aiavatar.aibackend.dto.GenerateScriptRequest;
import com.aiavatar.aibackend.dto.ScriptResponse;
import com.aiavatar.aibackend.dto.UpdateScriptRequest;
import com.aiavatar.aibackend.dto.VideoScriptResponse;
import com.aiavatar.aibackend.entity.Script;
import com.aiavatar.aibackend.service.ScriptService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/scripts")
public class ScriptController {

    private final ScriptService scriptService;

    public ScriptController(ScriptService scriptService) {
        this.scriptService = scriptService;
    }

    // Create a script
    @PostMapping
    public ResponseEntity<ScriptResponse> createScript(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateScriptRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        Script script = scriptService.createScript(
                email,
                projectId,
                request.getContent()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(script));
    }

    // Get all scripts for a project
    @GetMapping
    public ResponseEntity<List<ScriptResponse>> getScripts(
            @PathVariable Long projectId,
            Authentication authentication) {

        String email = authentication.getName();

        List<ScriptResponse> scripts = scriptService
                .getScripts(email, projectId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(scripts);
    }

    // Get one script
    @GetMapping("/{scriptId}")
    public ResponseEntity<ScriptResponse> getScript(
            @PathVariable Long projectId,
            @PathVariable Long scriptId,
            Authentication authentication) {

        String email = authentication.getName();

        Script script = scriptService.getScript(
                scriptId,
                email,
                projectId
        );

        return ResponseEntity.ok(toResponse(script));
    }

    // Update a script
    @PutMapping("/{scriptId}")
    public ResponseEntity<ScriptResponse> updateScript(
            @PathVariable Long projectId,
            @PathVariable Long scriptId,
            @Valid @RequestBody UpdateScriptRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        Script script = scriptService.updateScript(
                scriptId,
                email,
                projectId,
                request.getContent()
        );

        return ResponseEntity.ok(toResponse(script));
    }

    // Delete a script
    @DeleteMapping("/{scriptId}")
    public ResponseEntity<Void> deleteScript(
            @PathVariable Long projectId,
            @PathVariable Long scriptId,
            Authentication authentication) {

        String email = authentication.getName();

        scriptService.deleteScript(
                scriptId,
                email,
                projectId
        );

        return ResponseEntity.noContent().build();
    }

    // Convert Entity → Response DTO
    private ScriptResponse toResponse(Script script) {

        return new ScriptResponse(
                script.getId(),
                script.getContent(),
                script.getProject().getId(),
                script.getCreatedAt(),
                script.getUpdatedAt()
        );
    }
    
    
    @PostMapping("/generate")
    public ResponseEntity<ScriptResponse> generateScript(
            @PathVariable Long projectId,
            @Valid @RequestBody GenerateScriptRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        Script script = scriptService.generateScript(
                email,
                projectId,
                request.getPrompt()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(script));
    }
    
    @PostMapping("/generate-video-script")
    public ResponseEntity<VideoScriptResponse> generateVideoScript(
            @PathVariable Long projectId,
            @Valid @RequestBody GenerateScriptRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        VideoScriptResponse response =
                scriptService.generateVideoScript(
                        email,
                        projectId,
                        request.getPrompt()
                );

        return ResponseEntity.ok(response);
    }
}