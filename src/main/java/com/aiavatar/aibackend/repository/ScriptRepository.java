package com.aiavatar.aibackend.repository;

import com.aiavatar.aibackend.entity.Project;
import com.aiavatar.aibackend.entity.Script;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScriptRepository extends JpaRepository<Script, Long> {

    List<Script> findByProject(Project project);

    Optional<Script> findByIdAndProject(Long id, Project project);
}