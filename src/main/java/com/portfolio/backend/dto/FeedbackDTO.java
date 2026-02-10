package com.portfolio.backend.dto;

import com.portfolio.backend.entity.FeedbackCategory;
import com.portfolio.backend.entity.FeedbackPriority;
import com.portfolio.backend.entity.FeedbackStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FeedbackDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String userRole;
    private FeedbackCategory category;
    private String subject;
    private String message;
    private FeedbackPriority priority;
    private FeedbackStatus status;
    private String attachmentUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<FeedbackReplyDTO> replies;
}
