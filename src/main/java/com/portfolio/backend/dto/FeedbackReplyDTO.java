package com.portfolio.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FeedbackReplyDTO {
    private Long id;
    private Long feedbackId;
    private Long adminId;
    private String adminName;
    private String replyContent;
    private LocalDateTime createdAt;
}
