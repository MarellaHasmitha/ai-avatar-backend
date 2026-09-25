package com.aiavatar.aibackend.repository;

import com.aiavatar.aibackend.entity.Project;
import com.aiavatar.aibackend.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByUser(User user);

    Optional<Project> findByIdAndUser(Long id, User user);
}