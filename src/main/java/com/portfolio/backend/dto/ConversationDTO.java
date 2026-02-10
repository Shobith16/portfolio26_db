package com.portfolio.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConversationDTO {
    private Long id;
    private Long otherParticipantId;
    private String otherParticipantName;
    private String otherParticipantRole;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private long unreadCount;
}
