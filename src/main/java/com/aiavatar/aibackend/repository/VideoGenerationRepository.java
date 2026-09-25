package com.aiavatar.aibackend.repository;

import com.aiavatar.aibackend.entity.Project;
import com.aiavatar.aibackend.entity.VideoGeneration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VideoGenerationRepository
        extends JpaRepository<VideoGeneration, Long> {

    List<VideoGeneration> findByProject(Project project);

    Optional<VideoGeneration> findByIdAndProject(
            Long id,
            Project project
    );
}