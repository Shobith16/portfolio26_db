package com.portfolio.backend.service;

import com.portfolio.backend.dto.ChatMessageDTO;
import com.portfolio.backend.dto.ConversationDTO;
import com.portfolio.backend.entity.*;
import com.portfolio.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessagingService {
    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public java.util.Map<String, Object> sendMessage(Long senderId, Long receiverId, String content) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", 0L);
        response.put("conversationId", 0L);
        response.put("senderId", senderId);
        response.put("senderName", "Admin");
        response.put("content", content);
        response.put("createdAt", java.time.LocalDateTime.now());
        response.put("isSentByMe", true);
        return response;
    }

    @Transactional
    public Map<String, Object> sendMessageToEmail(Long senderId, String recipientEmail, String content) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        // Return email details for user to send manually
        String senderName = (sender != null ? sender.getFullName() : "Admin");
        String subject = "Message from " + senderName;
        String emailBody = "Dear Recruiter,\n\n" +
                content + "\n\n" +
                "Best regards,\n" + senderName;

        Map<String, Object> response = new HashMap<>();
        response.put("subject", subject);
        response.put("body", emailBody);
        response.put("mailtoLink", generateMailtoLink(recipientEmail, subject, emailBody));
        response.put("gmailLink", generateGmailLink(recipientEmail, subject, emailBody));

        return response;
    }

    private String generateGmailLink(String to, String subject, String body) {
        try {
            // Encode recipient as well to be safe
            String encodedTo = java.net.URLEncoder.encode(to, "UTF-8").replace("+", "%20");
            String encodedSubject = java.net.URLEncoder.encode(subject, "UTF-8").replace("+", "%20");
            String encodedBody = java.net.URLEncoder.encode(body, "UTF-8").replace("+", "%20");
            return String.format("https://mail.google.com/mail/?view=cm&fs=1&to=%s&su=%s&body=%s", encodedTo,
                    encodedSubject, encodedBody);
        } catch (Exception e) {
            return null;
        }
    }

    private String generateMailtoLink(String to, String subject, String body) {
        try {
            // Standard mailto encoding: spaces as %20, newlines as %0D%0A
            String encodedSubject = java.net.URLEncoder.encode(subject, "UTF-8").replace("+", "%20");
            String encodedBody = java.net.URLEncoder.encode(body, "UTF-8").replace("+", "%20").replace("%0A", "%0D%0A");
            return String.format("mailto:%s?subject=%s&body=%s", to, encodedSubject, encodedBody);
        } catch (Exception e) {
            return "mailto:" + to;
        }
    }

    public List<ConversationDTO> getUserConversations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Conversation> conversations = conversationRepository.findAllByUser(user);
        return conversations.stream()
                .map(conv -> mapConversationToDTO(conv, user))
                .collect(Collectors.toList());
    }

    public List<ChatMessageDTO> getConversationMessages(Long conversationId, Long currentUserId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        List<ChatMessage> messages = chatMessageRepository.findByConversationOrderByCreatedAtAsc(conversation);
        return messages.stream()
                .map(msg -> mapToDTO(msg, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSender().getId().equals(userId) && message.getReadAt() == null) {
            message.setReadAt(LocalDateTime.now());
            chatMessageRepository.save(message);
        }
    }

    private ChatMessageDTO mapToDTO(ChatMessage message, Long currentUserId) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversation().getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getFullName());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setReadAt(message.getReadAt());
        dto.setSentByMe(message.getSender().getId().equals(currentUserId));
        return dto;
    }

    private ConversationDTO mapConversationToDTO(Conversation conv, User currentUser) {
        User otherUser = conv.getParticipant1().getId().equals(currentUser.getId())
                ? conv.getParticipant2()
                : conv.getParticipant1();

        ConversationDTO dto = new ConversationDTO();
        dto.setId(conv.getId());
        dto.setOtherParticipantId(otherUser.getId());
        dto.setOtherParticipantName(otherUser.getFullName());
        dto.setOtherParticipantRole(otherUser.getRole());
        dto.setLastMessageAt(conv.getLastMessageAt());

        // Get last message preview
        List<ChatMessage> messages = chatMessageRepository.findByConversationOrderByCreatedAtAsc(conv);
        if (!messages.isEmpty()) {
            ChatMessage lastMsg = messages.get(messages.size() - 1);
            dto.setLastMessage(lastMsg.getContent().length() > 50
                    ? lastMsg.getContent().substring(0, 50) + "..."
                    : lastMsg.getContent());
        }

        // Count unread messages
        dto.setUnreadCount(chatMessageRepository.countByConversationAndReadAtIsNullAndSenderNot(conv, currentUser));

        return dto;
    }
}
