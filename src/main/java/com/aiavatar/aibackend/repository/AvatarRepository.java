package com.aiavatar.aibackend.repository;

import com.aiavatar.aibackend.entity.Avatar;
import com.aiavatar.aibackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {

    List<Avatar> findByUserOrderByCreatedAtDesc(User user);

    Optional<Avatar> findByIdAndUser(Long id, User user);
}