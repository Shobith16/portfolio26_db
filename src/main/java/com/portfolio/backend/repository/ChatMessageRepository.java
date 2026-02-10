package com.portfolio.backend.repository;

import com.portfolio.backend.entity.ChatMessage;
import com.portfolio.backend.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByConversationOrderByCreatedAtAsc(Conversation conversation);

    long countByConversationAndReadAtIsNullAndSenderNot(Conversation conversation,
            com.portfolio.backend.entity.User user);
}
