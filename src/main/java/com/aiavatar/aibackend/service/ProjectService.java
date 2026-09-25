package com.aiavatar.aibackend.service;

import com.aiavatar.aibackend.entity.Project;
import com.aiavatar.aibackend.entity.User;
import com.aiavatar.aibackend.exception.ResourceNotFoundException;
import com.aiavatar.aibackend.repository.ProjectRepository;
import com.aiavatar.aibackend.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            UserRepository userRepository) {

        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public Project createProject(
            String email,
            String name,
            String description) {

        User user = findUserByEmail(email);

        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setUser(user);

        return projectRepository.save(project);
    }

    public List<Project> getAllProjects(String email) {

        User user = findUserByEmail(email);

        return projectRepository.findByUser(user);
    }
    
    public Project getProject(Long projectId, String email) {

        User user = findUserByEmail(email);

        return projectRepository
                .findByIdAndUser(projectId, user)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found"));
    }
  

    public Project updateProject(
            Long projectId,
            String email,
            String name,
            String description) {

        Project project = getProject(projectId, email);

        project.setName(name);
        project.setDescription(description);

        return projectRepository.save(project);
    }

    public void deleteProject(Long projectId, String email) {

        Project project = getProject(projectId, email);

        projectRepository.delete(project);
    }

    private User findUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }
}