package com.aiavatar.aibackend.service;

import com.aiavatar.aibackend.entity.Project;
import com.aiavatar.aibackend.entity.Script;
import com.aiavatar.aibackend.entity.User;
import com.aiavatar.aibackend.exception.ResourceNotFoundException;
import com.aiavatar.aibackend.repository.ProjectRepository;
import com.aiavatar.aibackend.repository.ScriptRepository;
import com.aiavatar.aibackend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class VoiceGenerationService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ScriptRepository scriptRepository;
    private final VoiceGenerator voiceGenerator;

    public VoiceGenerationService(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            ScriptRepository scriptRepository,
            VoiceGenerator voiceGenerator) {

        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.scriptRepository = scriptRepository;
        this.voiceGenerator = voiceGenerator;
    }

    public byte[] generateVoice(
            String email,
            Long projectId,
            Long scriptId,
            String voiceId) {

        // 1. Find the logged-in user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // 2. Make sure the project belongs to this user
        Project project = projectRepository
                .findByIdAndUser(projectId, user)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found"));

        // 3. Make sure the script belongs to this project
        Script script = scriptRepository
                .findByIdAndProject(scriptId, project)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Script not found"));

     // 4. Send the script content and selected voice to the configured TTS provider
        return voiceGenerator.generateVoice(
                script.getContent(),
                voiceId
        );
    }
}