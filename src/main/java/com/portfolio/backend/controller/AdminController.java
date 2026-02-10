package com.portfolio.backend.controller;

import com.portfolio.backend.entity.*;
import com.portfolio.backend.service.PortfolioService;
import com.portfolio.backend.service.AppConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private AppConfigService appConfigService;

    @Autowired
    private com.portfolio.backend.service.MessagingService messagingService;

    @Autowired
    private com.portfolio.backend.service.NotificationService notificationService;

    @Autowired
    private com.portfolio.backend.service.FeedbackService feedbackService;

    // --- Authenticated GET Endpoints ---
    @GetMapping("/skills")
    @PreAuthorize("isAuthenticated()")
    public List<Skill> getSkills() {
        return portfolioService.getAllSkills();
    }

    @GetMapping("/experiences")
    @PreAuthorize("isAuthenticated()")
    public List<Experience> getExperiences() {
        return portfolioService.getAllExperiences();
    }

    // --- Secured Endpoints ---

    // Skills
    @PostMapping("/skills")
    @PreAuthorize("isAuthenticated()")
    public Skill createSkill(@RequestBody Skill skill) {
        return portfolioService.saveSkill(skill);
    }

    @DeleteMapping("/skills/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteSkill(@PathVariable Long id) {
        portfolioService.deleteSkill(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/skills/{id}")
    @PreAuthorize("isAuthenticated()")
    public Skill updateSkill(@PathVariable Long id, @RequestBody Skill skill) {
        skill.setId(id);
        return portfolioService.saveSkill(skill);
    }

    // Experiences
    @PostMapping("/experiences")
    @PreAuthorize("isAuthenticated()")
    public Experience createExperience(@RequestBody Experience exp) {
        return portfolioService.saveExperience(exp);
    }

    @DeleteMapping("/experiences/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteExperience(@PathVariable Long id) {
        portfolioService.deleteExperience(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/experiences/{id}")
    @PreAuthorize("isAuthenticated()")
    public Experience updateExperience(@PathVariable Long id, @RequestBody Experience exp) {
        exp.setId(id);
        return portfolioService.saveExperience(exp);
    }

    // Messages (Inbox)
    @GetMapping("/messages")
    @PreAuthorize("isAuthenticated()")
    public List<Message> getMessages() {
        return portfolioService.getAllMessages();
    }

    // Settings
    @GetMapping("/settings")
    public List<Setting> getSettings() {
        return portfolioService.getAllSettings();
    }

    @PostMapping("/settings")
    @PreAuthorize("isAuthenticated()")
    public Setting saveSetting(@RequestBody Setting setting) {
        return portfolioService.saveSetting(setting);
    }

    // Profile
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public Profile getProfile() {
        return portfolioService.getProfile();
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public Profile updateProfile(@RequestBody Profile profile) {
        return portfolioService.updateProfile(profile);
    }

    @Autowired
    private com.portfolio.backend.service.GoogleDriveService googleDriveService;

    @PostMapping("/photo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> uploadPhoto(@RequestParam("file") MultipartFile file) {
        try {
            // Use generic upload logic
            String gdriveUrl = googleDriveService.uploadFile(file);

            // Create FileMetadata object and save in DB
            FileMetadata metadata = new FileMetadata();
            metadata.setFileName(gdriveUrl); // Store the Drive URL directly
            metadata.setFileType(file.getContentType());
            metadata.setFileSize(file.getSize());
            metadata.setUploadConfig(appConfigService.getUploadConfig());

            // Automatically update the profile with the new photo metadata
            portfolioService.updateProfilePhoto(metadata);

            return ResponseEntity.ok(Map.of("url", gdriveUrl));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to upload file to Google Drive",
                    "message", e.getMessage() != null ? e.getMessage() : "Unknown error",
                    "type", e.getClass().getSimpleName()));
        }
    }

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Google Drive Storage
            String gdriveUrl = googleDriveService.uploadFile(file);

            // We return the URL directly for projects/other uses
            return ResponseEntity.ok(Map.of("url", gdriveUrl));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to upload file to Google Drive",
                    "message", e.getMessage() != null ? e.getMessage() : "Unknown error",
                    "type", e.getClass().getSimpleName()));
        }
    }

    // --- Super Admin Endpoints ---
    @GetMapping("/users")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<User> getAllUsers() {
        return portfolioService.getAllUsers();
    }

    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusMap,
            org.springframework.security.core.Authentication auth,
            jakarta.servlet.http.HttpServletRequest request) {

        Long adminId = ((com.portfolio.backend.security.UserDetailsImpl) auth.getPrincipal()).getId();
        String ip = request.getRemoteAddr();
        String ua = request.getHeader("User-Agent");
        return portfolioService.updateUserStatus(id, statusMap.get("status"), adminId, ip, ua);
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody User userDetails,
            org.springframework.security.core.Authentication auth,
            jakarta.servlet.http.HttpServletRequest request) {

        Long adminId = ((com.portfolio.backend.security.UserDetailsImpl) auth.getPrincipal()).getId();
        String ip = request.getRemoteAddr();
        String ua = request.getHeader("User-Agent");
        return portfolioService.updateUser(id, userDetails, adminId, ip, ua);
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<AuditLog> getAllAuditLogs() {
        return portfolioService.getAllAuditLogs();
    }

    @GetMapping("/platform-settings")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getPlatformSettings() {
        String uiStyle = portfolioService.getSettingValue("home_ui_style");
        return ResponseEntity.ok(Map.of(
                "allowRegistration", true,
                "requireEmailVerification", false,
                "maintenanceMode", false,
                "home_ui_style", uiStyle != null ? uiStyle : "classic"));
    }

    @PostMapping("/platform-settings")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> updatePlatformSettings(@RequestBody Map<String, Object> settings) {
        // Save each setting to the database
        settings.forEach((key, value) -> {
            Setting setting = new Setting();
            setting.setKey(key);
            setting.setValue(value.toString());
            portfolioService.saveSetting(setting);
        });
        return ResponseEntity.ok(settings);
    }

    @GetMapping("/super-admin/metrics")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<com.portfolio.backend.dto.SuperAdminMetricsDTO> getSuperAdminMetrics() {
        return ResponseEntity.ok(analyticsService.getSuperAdminMetrics());
    }

    // --- Analytics Endpoints ---
    @Autowired
    private com.portfolio.backend.service.AnalyticsService analyticsService;

    @GetMapping("/analytics/daily-stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getDailyStats(org.springframework.security.core.Authentication auth) {
        try {
            if (auth == null || auth.getPrincipal() == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
            }

            Long userId;
            if (auth.getPrincipal() instanceof com.portfolio.backend.security.UserDetailsImpl) {
                userId = ((com.portfolio.backend.security.UserDetailsImpl) auth.getPrincipal()).getId();
            } else {
                return ResponseEntity.status(500).body(Map.of("error", "Invalid principal type"));
            }

            return ResponseEntity.ok(analyticsService.getDailyStats(userId, 7));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch stats", "message", e.getMessage()));
        }
    }

    // Contact Info
    @GetMapping("/contact")
    @PreAuthorize("isAuthenticated()")
    public ContactInfo getContact() {
        return portfolioService.getContactInfo();
    }

    @PutMapping("/contact")
    @PreAuthorize("isAuthenticated()")
    public ContactInfo updateContact(@RequestBody ContactInfo contactInfo) {
        return portfolioService.updateContactInfo(contactInfo);
    }

    // ===== MESSAGING ENDPOINTS =====
    @PostMapping("/messages/send")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> sendMessage(
            @RequestBody Map<String, Object> payload,
            org.springframework.security.core.Authentication auth) {
        try {
            Long senderId = ((com.portfolio.backend.security.UserDetailsImpl) auth.getPrincipal()).getId();
            Long receiverId = Long.valueOf(payload.get("receiverId").toString());
            String content = payload.get("content").toString();
            return ResponseEntity.ok(messagingService.sendMessage(senderId, receiverId, content));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/messages/send-email")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> sendMessageToEmail(
            @RequestBody Map<String, Object> payload,
            org.springframework.security.core.Authentication auth) {
        try {
            Long senderId = ((com.portfolio.backend.security.UserDetailsImpl) auth.getPrincipal()).getId();
            String recipientEmail = payload.get("email").toString();
            String content = payload.get("content").toString();
            return ResponseEntity.ok(messagingService.sendMessageToEmail(senderId, recipientEmail, content));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/messages/conversations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getConversations(org.springframework.security.core.Authentication auth) {
        try {
            Long userId = ((com.portfolio.backend.security.UserDetailsImpl) auth.getPrincipal()).getId();
            return ResponseEntity.ok(messagingService.getUserConversations(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/messages/conversation/{conversationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getConversationMessages(
            @PathVariable Long conversationId,
            org.springframework.security.core.Authentication auth) {
        try {
            Long userId = ((com.portfolio.backend.security.UserDetailsImpl) auth.getPrincipal()).getId();
            return ResponseEntity.ok(messagingService.getConversationMessages(conversationId, userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/messages/{messageId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> markMessageAsRead(
            @PathVariable Long messageId,
            org.springframework.security.core.Authentication auth) {
        try {
            Long userId = ((com.portfolio.backend.security.UserDetailsImpl) auth.getPrincipal()).getId();
            messagingService.markAsRead(messageId, userId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ===== NOTIFICATION ENDPOINTS =====
    @GetMapping("/notifications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getNotifications(org.springframework.security.core.Authentication auth) {
        try {
            Long userId = ((com.portfolio.backend.security.UserDetailsImpl) auth.getPrincipal()).getId();
            return ResponseEntity.ok(notificationService.getUserNotifications(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/notifications/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getRecentNotifications(org.springframework.security.core.Authentication auth) {
        try {
            Long userId = ((com.portfolio.backend.security.UserDetailsImpl) auth.getPrincipal()).getId();
            return ResponseEntity.ok(notificationService.getRecentNotifications(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/notifications/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUnreadCount(org.springframework.security.core.Authentication auth) {
        try {
            Long userId = ((com.portfolio.backend.security.UserDetailsImpl) auth.getPrincipal()).getId();
            return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(userId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/notifications/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable Long notificationId) {
        try {
            notificationService.markAsRead(notificationId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/notifications/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> markAllNotificationsAsRead(org.springframework.security.core.Authentication auth) {
        try {
            Long userId = ((com.portfolio.backend.security.UserDetailsImpl) auth.getPrincipal()).getId();
            notificationService.markAllAsRead(userId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ===== FEEDBACK ENDPOINTS =====
    @PostMapping("/feedback")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> submitFeedback(
            @RequestBody Map<String, Object> payload,
            org.springframework.security.core.Authentication auth) {
        try {
            Long userId = ((com.portfolio.backend.security.UserDetailsImpl) auth.getPrincipal()).getId();
            com.portfolio.backend.entity.FeedbackCategory category = com.portfolio.backend.entity.FeedbackCategory
                    .valueOf(payload.get("category").toString());
            String subject = payload.get("subject").toString();
            String message = payload.get("message").toString();
            com.portfolio.backend.entity.FeedbackPriority priority = com.portfolio.backend.entity.FeedbackPriority
                    .valueOf(
                            payload.getOrDefault("priority", "MEDIUM").toString());
            String attachmentUrl = payload.get("attachmentUrl") != null ? payload.get("attachmentUrl").toString()
                    : null;

            return ResponseEntity.ok(feedbackService.submitFeedback(
                    userId, category, subject, message, priority, attachmentUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/feedback")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserFeedback(org.springframework.security.core.Authentication auth) {
        try {
            Long userId = ((com.portfolio.backend.security.UserDetailsImpl) auth.getPrincipal()).getId();
            return ResponseEntity.ok(feedbackService.getUserFeedback(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/feedback/all")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getAllFeedback() {
        try {
            return ResponseEntity.ok(feedbackService.getAllFeedback());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/feedback/{feedbackId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getFeedbackById(@PathVariable Long feedbackId) {
        try {
            return ResponseEntity.ok(feedbackService.getFeedbackById(feedbackId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/feedback/{feedbackId}/reply")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> replyToFeedback(
            @PathVariable Long feedbackId,
            @RequestBody Map<String, String> payload,
            org.springframework.security.core.Authentication auth,
            jakarta.servlet.http.HttpServletRequest request) {
        try {
            Long adminId = ((com.portfolio.backend.security.UserDetailsImpl) auth.getPrincipal()).getId();
            String replyContent = payload.get("replyContent");
            String ip = request.getRemoteAddr();
            String ua = request.getHeader("User-Agent");
            return ResponseEntity.ok(feedbackService.replyToFeedback(feedbackId, adminId, replyContent, ip, ua));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/feedback/{feedbackId}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateFeedbackStatus(
            @PathVariable Long feedbackId,
            @RequestBody Map<String, String> payload,
            org.springframework.security.core.Authentication auth,
            jakarta.servlet.http.HttpServletRequest request) {
        try {
            Long adminId = ((com.portfolio.backend.security.UserDetailsImpl) auth.getPrincipal()).getId();
            String ip = request.getRemoteAddr();
            String ua = request.getHeader("User-Agent");
            com.portfolio.backend.entity.FeedbackStatus status = com.portfolio.backend.entity.FeedbackStatus
                    .valueOf(payload.get("status"));
            feedbackService.updateFeedbackStatus(feedbackId, status, adminId, ip, ua);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
