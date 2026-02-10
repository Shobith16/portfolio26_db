package com.portfolio.backend.repository;

import com.portfolio.backend.entity.Feedback;
import com.portfolio.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByUserOrderByCreatedAtDesc(User user);
}
