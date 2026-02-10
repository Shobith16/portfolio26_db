package com.portfolio.backend.repository;

import com.portfolio.backend.entity.Conversation;
import com.portfolio.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("SELECT c FROM Conversation c WHERE (c.participant1 = :u1 AND c.participant2 = :u2) OR (c.participant1 = :u2 AND c.participant2 = :u1)")
    Optional<Conversation> findBetweenUsers(@Param("u1") User u1, @Param("u2") User u2);

    @Query("SELECT c FROM Conversation c WHERE c.participant1 = :user OR c.participant2 = :user ORDER BY c.lastMessageAt DESC")
    List<Conversation> findAllByUser(@Param("user") User user);
}
