package com.portfolio.backend.dto;

import com.portfolio.backend.entity.NotificationType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Long id;
    private NotificationType type;
    private String title;
    private String content;
    private boolean isRead;
    private Long relatedId;
    private LocalDateTime createdAt;
}
