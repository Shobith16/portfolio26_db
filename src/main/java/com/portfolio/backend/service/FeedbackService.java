package com.portfolio.backend.service;

import com.portfolio.backend.dto.FeedbackDTO;
import com.portfolio.backend.dto.FeedbackReplyDTO;
import com.portfolio.backend.entity.*;
import com.portfolio.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final FeedbackReplyRepository feedbackReplyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Transactional
    public FeedbackDTO submitFeedback(Long userId, FeedbackCategory category, String subject,
            String message, FeedbackPriority priority, String attachmentUrl) {
        if (userId == null) {
            throw new RuntimeException("User ID cannot be null");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setCategory(category);
        feedback.setSubject(subject);
        feedback.setMessage(message);
        feedback.setPriority(priority);
        feedback.setAttachmentUrl(attachmentUrl);
        feedback.setStatus(FeedbackStatus.SUBMITTED);
        feedback.setCreatedAt(LocalDateTime.now());

        Feedback saved = feedbackRepository.save(feedback);
        return mapToDTO(saved);
    }

    public List<FeedbackDTO> getUserFeedback(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Feedback> feedbackList = feedbackRepository.findByUserOrderByCreatedAtDesc(user);
        return feedbackList.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<FeedbackDTO> getAllFeedback() {
        List<Feedback> feedbackList = feedbackRepository.findAll();
        return feedbackList.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public FeedbackDTO getFeedbackById(Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
        return mapToDTO(feedback);
    }

    @Transactional
    public FeedbackReplyDTO replyToFeedback(Long feedbackId, Long adminId, String replyContent, String ip, String ua) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        FeedbackReply reply = new FeedbackReply();
        reply.setFeedback(feedback);
        reply.setAdmin(admin);
        reply.setReplyContent(replyContent);
        reply.setCreatedAt(LocalDateTime.now());
        FeedbackReply saved = feedbackReplyRepository.save(reply);

        // Update feedback status
        feedback.setStatus(FeedbackStatus.RESPONDED);
        feedback.setUpdatedAt(LocalDateTime.now());
        feedbackRepository.save(feedback);

        // Notify user
        notificationService.createFeedbackReplyNotification(feedback.getUser(), feedbackId);

        // Audit Log
        String details = "Replied to feedback #" + feedbackId + ": "
                + (replyContent.length() > 50 ? replyContent.substring(0, 50) + "..." : replyContent);
        auditLogService.log(admin, "REPLY_FEEDBACK", "FEEDBACK", String.valueOf(feedbackId), details, ip, ua);

        return mapReplyToDTO(saved);
    }

    @Transactional
    public void updateFeedbackStatus(Long feedbackId, FeedbackStatus status, Long adminId, String ip, String ua) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));

        FeedbackStatus oldStatus = feedback.getStatus();
        feedback.setStatus(status);
        feedback.setUpdatedAt(LocalDateTime.now());
        feedbackRepository.save(feedback);

        // Audit Log
        User admin = userRepository.findById(adminId).orElse(null);
        String details = "Updated feedback #" + feedbackId + " status from " + oldStatus + " to " + status;
        auditLogService.log(admin, "UPDATE_FEEDBACK_STATUS", "FEEDBACK", String.valueOf(feedbackId), details, ip, ua);
    }

    private FeedbackDTO mapToDTO(Feedback feedback) {
        FeedbackDTO dto = new FeedbackDTO();
        dto.setId(feedback.getId());
        dto.setUserId(feedback.getUser().getId());
        dto.setUserName(feedback.getUser().getFullName());
        dto.setUserRole(feedback.getUser().getRole());
        dto.setCategory(feedback.getCategory());
        dto.setSubject(feedback.getSubject());
        dto.setMessage(feedback.getMessage());
        dto.setPriority(feedback.getPriority());
        dto.setStatus(feedback.getStatus());
        dto.setAttachmentUrl(feedback.getAttachmentUrl());
        dto.setCreatedAt(feedback.getCreatedAt());
        dto.setUpdatedAt(feedback.getUpdatedAt());

        // Load replies
        List<FeedbackReply> replies = feedbackReplyRepository.findByFeedbackOrderByCreatedAtAsc(feedback);
        dto.setReplies(replies.stream().map(this::mapReplyToDTO).collect(Collectors.toList()));

        return dto;
    }

    private FeedbackReplyDTO mapReplyToDTO(FeedbackReply reply) {
        FeedbackReplyDTO dto = new FeedbackReplyDTO();
        dto.setId(reply.getId());
        dto.setFeedbackId(reply.getFeedback().getId());
        dto.setAdminId(reply.getAdmin().getId());
        dto.setAdminName(reply.getAdmin().getFullName());
        dto.setReplyContent(reply.getReplyContent());
        dto.setCreatedAt(reply.getCreatedAt());
        return dto;
    }
}
