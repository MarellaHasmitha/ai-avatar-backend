package com.aiavatar.aibackend.service;

import com.aiavatar.aibackend.dto.VideoScriptResponse;
import com.aiavatar.aibackend.entity.Project;
import com.aiavatar.aibackend.entity.Script;
import com.aiavatar.aibackend.entity.User;
import com.aiavatar.aibackend.exception.ResourceNotFoundException;
import com.aiavatar.aibackend.repository.ProjectRepository;
import com.aiavatar.aibackend.repository.ScriptRepository;
import com.aiavatar.aibackend.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScriptService {

    private final ScriptRepository scriptRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ScriptGenerator scriptGenerator;

    public ScriptService(
            ScriptRepository scriptRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            ScriptGenerator scriptGenerator) {

        this.scriptRepository = scriptRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.scriptGenerator = scriptGenerator;
    }

    // Create a script manually inside a project
    public Script createScript(
            String email,
            Long projectId,
            String content) {

        Project project = findProjectForUser(projectId, email);

        Script script = new Script();
        script.setContent(content);
        script.setProject(project);

        return scriptRepository.save(script);
    }

    // Generate a script using AI and save it inside the project
    public Script generateScript(
            String email,
            Long projectId,
            String prompt) {

        // Make sure the project belongs to the logged-in user
        Project project = findProjectForUser(projectId, email);

        // Send the prompt to the configured AI provider
        String generatedContent = scriptGenerator.generateScript(prompt);

        // Create a new Script entity
        Script script = new Script();
        script.setContent(generatedContent);
        script.setProject(project);

        // Save the generated script in PostgreSQL
        return scriptRepository.save(script);
    }

    // Get all scripts belonging to a project
    public List<Script> getScripts(
            String email,
            Long projectId) {

        Project project = findProjectForUser(projectId, email);

        return scriptRepository.findByProject(project);
    }

    // Get one script
    public Script getScript(
            Long scriptId,
            String email,
            Long projectId) {

        Project project = findProjectForUser(projectId, email);

        return scriptRepository
                .findByIdAndProject(scriptId, project)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Script not found"));
    }

    // Update a script
    public Script updateScript(
            Long scriptId,
            String email,
            Long projectId,
            String content) {

        Script script = getScript(scriptId, email, projectId);

        script.setContent(content);

        return scriptRepository.save(script);
    }

    // Delete a script
    public void deleteScript(
            Long scriptId,
            String email,
            Long projectId) {

        Script script = getScript(scriptId, email, projectId);

        scriptRepository.delete(script);
    }

    // Find project only if it belongs to the logged-in user
    private Project findProjectForUser(
            Long projectId,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return projectRepository
                .findByIdAndUser(projectId, user)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found"));
    }
    
    
    public VideoScriptResponse generateVideoScript(
            String email,
            Long projectId,
            String prompt) {

        Project project = findProjectForUser(projectId, email);

        return scriptGenerator.generateVideoScript(prompt);
    }
    
    
}