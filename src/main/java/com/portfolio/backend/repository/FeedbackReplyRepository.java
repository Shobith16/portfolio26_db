package com.portfolio.backend.repository;

import com.portfolio.backend.entity.Feedback;
import com.portfolio.backend.entity.FeedbackReply;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeedbackReplyRepository extends JpaRepository<FeedbackReply, Long> {
    List<FeedbackReply> findByFeedbackOrderByCreatedAtAsc(Feedback feedback);
}
