package com.portfolio.backend.controller;

import com.portfolio.backend.entity.*;
import com.portfolio.backend.repository.UserRepository;
import com.portfolio.backend.service.PortfolioService;
import com.portfolio.backend.service.VisitorTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class PublicPortfolioController {

    private final UserRepository userRepository;
    private final PortfolioService portfolioService;
    private final VisitorTrackingService trackingService;
    private final com.portfolio.backend.service.GoogleDriveService googleDriveService;

    @GetMapping("/images/drive/{fileId}")
    public ResponseEntity<byte[]> getDriveImage(@PathVariable String fileId) {
        try {
            byte[] imageData = googleDriveService.downloadFile(fileId);
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "image/jpeg") // Most images will be jpeg
                                                                                             // or png
                    .header(org.springframework.http.HttpHeaders.CACHE_CONTROL, "public, max-age=31536000") // Cache for
                                                                                                            // 1 year
                    .body(imageData);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{username}/profile")
    public ResponseEntity<Profile> getProfile(@PathVariable String username,
            jakarta.servlet.http.HttpServletRequest request) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    trackingService.trackView(user, request.getRemoteAddr(), request.getHeader("User-Agent"),
                            request.getHeader("Referer"));
                    return ResponseEntity.ok(portfolioService.getProfileByUserId(user.getId()));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{username}/update-view-duration")
    public ResponseEntity<?> updateDuration(@PathVariable String username, @RequestBody Map<String, Integer> payload) {
        // Find latest view and update duration - for simplicity, we could pass viewId
        // from frontend
        // For now, let's assume we might need a separate track-view endpoint to return
        // a viewId
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/skills")
    public ResponseEntity<List<Skill>> getSkills(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(user -> ResponseEntity.ok(portfolioService.getPublicSkillsByUserId(user.getId())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{username}/experiences")
    public ResponseEntity<List<Experience>> getExperience(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(user -> ResponseEntity.ok(portfolioService.getExperiencesByUserId(user.getId())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{username}/projects")
    public ResponseEntity<List<Project>> getProjects(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(user -> ResponseEntity.ok(portfolioService.getPublicProjectsByUserId(user.getId())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{username}/contact")
    public ResponseEntity<ContactInfo> getContactInfo(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(user -> ResponseEntity.ok(portfolioService.getContactInfoByUserId(user.getId())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{username}/contact")
    public ResponseEntity<Message> sendMessage(@PathVariable String username, @RequestBody Message message) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    message.setUser(user);
                    return ResponseEntity.ok(portfolioService.saveMessage(message));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // --- Analytics Tracking ---
    @Autowired
    private com.portfolio.backend.service.AnalyticsService analyticsService;

    @PostMapping("/{userId}/track-view")
    public ResponseEntity<?> trackView(
            @PathVariable Long userId,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @RequestHeader(value = "Referer", required = false) String referrer,
            jakarta.servlet.http.HttpServletRequest request) {
        analyticsService.recordView(userId, request.getRemoteAddr(), userAgent, referrer);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/track-share")
    public ResponseEntity<?> trackShare(
            @PathVariable Long userId,
            @RequestBody Map<String, String> body) {
        analyticsService.recordShare(userId, body.getOrDefault("platform", "Unknown"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/settings/ui")
    public ResponseEntity<Map<String, String>> getUiSettings() {
        String uiStyle = portfolioService.getSettingValue("home_ui_style");
        return ResponseEntity.ok(Map.of("ui_style", uiStyle != null ? uiStyle : "classic"));
    }
}
