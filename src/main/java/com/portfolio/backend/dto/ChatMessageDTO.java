package com.portfolio.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessageDTO {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private boolean isSentByMe;
}
