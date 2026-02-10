package com.portfolio.backend.repository;

import com.portfolio.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByOrderByCreatedAtDesc();

    List<Message> findByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByUserId(Long userId);
}
