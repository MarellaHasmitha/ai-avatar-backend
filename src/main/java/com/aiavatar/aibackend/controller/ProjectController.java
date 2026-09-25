package com.aiavatar.aibackend.controller;

import com.aiavatar.aibackend.dto.CreateProjectRequest;
import com.aiavatar.aibackend.dto.ProjectResponse;
import com.aiavatar.aibackend.dto.UpdateProjectRequest;
import com.aiavatar.aibackend.entity.Project;
import com.aiavatar.aibackend.service.ProjectService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        Project project = projectService.createProject(
                email,
                request.getName(),
                request.getDescription()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(project));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects(
            Authentication authentication) {

        String email = authentication.getName();

        List<ProjectResponse> projects = projectService
                .getAllProjects(email)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        Project project = projectService.getProject(id, email);

        return ResponseEntity.ok(toResponse(project));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        Project project = projectService.updateProject(
                id,
                email,
                request.getName(),
                request.getDescription()
        );

        return ResponseEntity.ok(toResponse(project));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        projectService.deleteProject(id, email);

        return ResponseEntity.noContent().build();
    }

    private ProjectResponse toResponse(Project project) {

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}