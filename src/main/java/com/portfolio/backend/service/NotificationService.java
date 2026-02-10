package com.portfolio.backend.service;

import com.portfolio.backend.dto.NotificationDTO;
import com.portfolio.backend.entity.Notification;
import com.portfolio.backend.entity.NotificationType;
import com.portfolio.backend.entity.User;
import com.portfolio.backend.repository.NotificationRepository;
import com.portfolio.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createMessageNotification(User receiver, User sender, Long messageId) {
        Notification notification = new Notification();
        notification.setUser(receiver);
        notification.setType(NotificationType.MESSAGE);
        notification.setTitle("New Message from " + sender.getFullName());
        notification.setContent(sender.getFullName() + " sent you a message");
        notification.setRelatedId(messageId);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Transactional
    public void createFeedbackReplyNotification(User user, Long feedbackId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(NotificationType.FEEDBACK_REPLY);
        notification.setTitle("Feedback Response Received");
        notification.setContent("An admin has responded to your feedback");
        notification.setRelatedId(feedbackId);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Transactional
    public void createSystemNotification(User user, String title, String content) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(NotificationType.SYSTEM);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    public List<NotificationDTO> getUserNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        return notifications.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<NotificationDTO> getRecentNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications = notificationRepository.findTop10ByUserOrderByCreatedAtDesc(user);
        return notifications.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Notification> unread = notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .filter(n -> !n.isRead())
                .collect(Collectors.toList());

        unread.forEach(n -> {
            n.setRead(true);
            n.setReadAt(LocalDateTime.now());
        });
        notificationRepository.saveAll(unread);
    }

    private NotificationDTO mapToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setRead(notification.isRead());
        dto.setRelatedId(notification.getRelatedId());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}
