package com.aiavatar.aibackend.repository;

import com.aiavatar.aibackend.entity.VideoGeneration;
import com.aiavatar.aibackend.entity.VideoScene;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VideoSceneRepository
        extends JpaRepository<VideoScene, Long> {

    List<VideoScene> findByVideoGenerationOrderBySceneNumberAsc(
            VideoGeneration videoGeneration
            
    );

    Optional<VideoScene> findByIdAndVideoGeneration(
            Long id,
            VideoGeneration videoGeneration
    );
}