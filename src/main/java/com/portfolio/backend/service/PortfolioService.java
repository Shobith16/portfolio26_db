package com.portfolio.backend.service;

import com.portfolio.backend.entity.*;
import com.portfolio.backend.repository.*;
import com.portfolio.backend.security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortfolioService {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioService.class);

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private ExperienceRepository experienceRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ContactInfoRepository contactInfoRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private AppConfigService appConfigService;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    // --- Helper to resolve Photo URL ---
    // --- Helper to resolve Photo URL ---
    private void resolvePhotoUrl(Profile profile) {
        if (profile != null && profile.getPhoto() != null) {
            profile.getPhoto().setUrl(resolveUrl(profile.getPhoto()));
        }
    }

    private String resolveUrl(FileMetadata metadata) {
        if (metadata == null)
            return null;
        String fileName = metadata.getFileName();

        // If it's already a full URL (like Google Drive), check format and use it
        if (fileName != null && (fileName.startsWith("http://") || fileName.startsWith("https://"))) {
            // Fix for 403 Forbidden: Convert drive.google.com/uc?id... to
            // lh3.googleusercontent.com/d/
            if (fileName.contains("drive.google.com/")) {
                String fileId = null;
                if (fileName.contains("id=")) {
                    fileId = fileName.substring(fileName.indexOf("id=") + 3);
                    if (fileId.contains("&")) {
                        fileId = fileId.substring(0, fileId.indexOf("&"));
                    }
                } else if (fileName.contains("/d/")) {
                    fileId = fileName.substring(fileName.indexOf("/d/") + 3);
                    if (fileId.contains("/")) {
                        fileId = fileId.substring(0, fileId.indexOf("/"));
                    }
                }

                if (fileId != null) {
                    String baseUrl = appConfigService.getBaseUrl();
                    return baseUrl + "/api/public/images/drive/" + fileId;
                }
            }
            return fileName;
        } else {
            String baseUrl = appConfigService.getBaseUrl();
            String uploadPath = "";

            if (metadata.getUploadConfig() != null) {
                uploadPath = metadata.getUploadConfig().getConfigValue();
            }

            return baseUrl + uploadPath + fileName;
        }
    }

    public void resolveProjectUrls(List<Project> projects) {
        if (projects != null) {
            projects.forEach(this::resolveProjectUrl);
        }
    }

    private void resolveProjectUrl(Project project) {
        if (project != null && project.getImageUrl() != null) {
            String imageUrl = project.getImageUrl();

            // Check if it's a Google Drive link that needs conversion (from uc?id= format)
            if (imageUrl.startsWith("http") && imageUrl.contains("drive.google.com/")) {
                String fileId = null;
                if (imageUrl.contains("id=")) {
                    fileId = imageUrl.substring(imageUrl.indexOf("id=") + 3);
                    if (fileId.contains("&")) {
                        fileId = fileId.substring(0, fileId.indexOf("&"));
                    }
                } else if (imageUrl.contains("/d/")) {
                    fileId = imageUrl.substring(imageUrl.indexOf("/d/") + 3);
                    if (fileId.contains("/")) {
                        fileId = fileId.substring(0, fileId.indexOf("/"));
                    }
                }

                if (fileId != null) {
                    String baseUrl = appConfigService.getBaseUrl();
                    project.setImageUrl(baseUrl + "/api/public/images/drive/" + fileId);
                    return;
                }
            }

            // If it's not a full URL and doesn't look like one, we might need to prepend
            // base URL
            if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                String baseUrl = appConfigService.getBaseUrl();
                // Assuming projects use the same default upload path if relative
                project.setImageUrl(baseUrl + "/api/public/uploads/" + imageUrl);
            }
        }
    }

    private void internalizePhotoUrl(Profile profile) {
        // Since we now use a structured relation with FileMetadata
        // and its path is derived from AppConfig, we don't need to
        // internalize a URL string into the profile anymore.
        // The link between Profile and FileMetadata handles this.
    }

    // --- Helper to get current authenticated user ---
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) principal).getId();
        }
        return null;
    }

    private User getCurrentUser() {
        Long userId = getCurrentUserId();
        if (userId == null)
            return null;
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setLastActiveAt(java.time.LocalDateTime.now());
            userRepository.save(user);
        }
        return user;
    }

    // --- Skills ---
    public List<Skill> getAllSkills() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }
        return skillRepository.findByUserIdOrderByOrderAsc(userId);
    }

    public List<Skill> getSkillsByUserId(Long userId) {
        return skillRepository.findByUserIdOrderByOrderAsc(userId);
    }

    public List<Skill> getPublicSkillsByUserId(Long userId) {
        return skillRepository.findByUserIdAndIsHiddenFalseOrderByOrderAsc(userId);
    }

    public Skill saveSkill(Skill skill) {
        User user = getCurrentUser();
        skill.setUser(user);
        return skillRepository.save(skill);
    }

    public void deleteSkill(Long id) {
        skillRepository.deleteById(id);
    }

    // --- Experiences ---
    public List<Experience> getAllExperiences() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }
        return experienceRepository.findByUserIdOrderByOrderAsc(userId);
    }

    public List<Experience> getExperiencesByUserId(Long userId) {
        return experienceRepository.findByUserIdOrderByOrderAsc(userId);
    }

    public Experience saveExperience(Experience exp) {
        User user = getCurrentUser();
        exp.setUser(user);
        return experienceRepository.save(exp);
    }

    public void deleteExperience(Long id) {
        experienceRepository.deleteById(id);
    }

    // --- Messages ---
    public List<Message> getAllMessages() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }
        return messageRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Message saveMessage(Message msg) {
        return messageRepository.save(msg);
    }

    public void deleteMessage(Long id) {
        messageRepository.deleteById(id);
    }

    // --- Settings ---
    public List<Setting> getAllSettings() {
        return settingRepository.findAll();
    }

    public Setting saveSetting(Setting setting) {
        return settingRepository.save(setting);
    }

    public String getSettingValue(String key) {
        return settingRepository.findById(key).map(Setting::getValue).orElse("");
    }

    // --- Profile ---
    public Profile getProfile() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }
        Profile profile = profileRepository.findByUserId(userId).orElse(null);
        resolvePhotoUrl(profile);
        return profile;
    }

    public Profile getProfileByUserId(Long userId) {
        Profile profile = profileRepository.findByUserId(userId).orElse(null);
        resolvePhotoUrl(profile);
        return profile;
    }

    public Profile updateProfile(Profile profile) {
        User user = getCurrentUser();
        logger.info("Updating profile for user: {}", user.getId());
        if (profile.getId() != null) {
            logger.debug("Profile ID from frontend: {}", profile.getId());
        } else {
            logger.warn("Profile ID is NULL - this might cause a duplicate insert!");
            // Try to find existing profile to update instead
            Profile existing = profileRepository.findByUserId(user.getId()).orElse(null);
            if (existing != null) {
                logger.info("Found existing profile with ID: {}. Updating that instead.", existing.getId());
                profile.setId(existing.getId());
            }
        }

        if (profile.getFullName() == null || profile.getFullName().trim().isEmpty()) {
            logger.info("Profile fullName is empty. internalizing from User entity.");
            if (user.getFullName() != null && !user.getFullName().isEmpty()) {
                profile.setFullName(user.getFullName());
            } else {
                profile.setFullName(user.getUsername());
            }
        }

        profile.setUser(user);
        internalizePhotoUrl(profile);
        Profile saved = profileRepository.save(profile);
        resolvePhotoUrl(saved);
        return saved;
    }

    public void updateProfilePhoto(FileMetadata metadata) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user: " + userId));

        // Save metadata first
        FileMetadata savedMetadata = fileMetadataRepository.save(metadata);

        profile.setPhoto(savedMetadata);
        profileRepository.save(profile);
    }

    // --- Contact Info ---
    public ContactInfo getContactInfo() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }
        return contactInfoRepository.findByUserId(userId).orElse(null);
    }

    public ContactInfo getContactInfoByUserId(Long userId) {
        return contactInfoRepository.findByUserId(userId).orElse(null);
    }

    public List<Project> getProjectsByUserId(Long userId) {
        return projectRepository.findByUserIdOrderByOrderAsc(userId);
    }

    public List<Project> getPublicProjectsByUserId(Long userId) {
        return projectRepository.findByUserIdAndIsHiddenFalseOrderByOrderAsc(userId);
    }

    public ContactInfo updateContactInfo(ContactInfo contactInfo) {
        User user = getCurrentUser();
        contactInfo.setUser(user);
        return contactInfoRepository.save(contactInfo);
    }

    // --- Super Admin Methods ---
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public ResponseEntity<?> updateUser(Long id, User updatedDetails, Long adminId, String ip, String ua) {
        return userRepository.findById(id).map(user -> {
            // Check for duplicates if username/email changed
            if (!user.getUsername().equals(updatedDetails.getUsername())
                    && userRepository.existsByUsername(updatedDetails.getUsername())) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "Username is already taken!"));
            }
            if (!user.getEmail().equals(updatedDetails.getEmail())
                    && userRepository.existsByEmail(updatedDetails.getEmail())) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "Email is already in use!"));
            }

            String oldRole = user.getRole();
            String oldStatus = user.getStatus().name();

            user.setFullName(updatedDetails.getFullName());
            user.setUsername(updatedDetails.getUsername());
            user.setEmail(updatedDetails.getEmail());
            user.setRole(updatedDetails.getRole());

            if (updatedDetails.getStatus() != null) {
                user.setStatus(updatedDetails.getStatus());
            }

            userRepository.save(user);

            // Audit Log
            User admin = userRepository.findById(adminId).orElse(null);
            String details = String.format("Updated user %s (ID: %d). Role: %s -> %s. Status: %s -> %s.",
                    user.getUsername(), user.getId(), oldRole, user.getRole(), oldStatus, user.getStatus());

            auditLogService.log(admin, "UPDATE_USER", "USER", String.valueOf(user.getId()), details, ip, ua);

            return ResponseEntity.ok(user);
        }).orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<?> updateUserStatus(Long userId, String statusStr, Long adminId, String ip, String ua) {
        return userRepository.findById(userId).map(user -> {
            UserStatus oldStatus = user.getStatus();
            UserStatus status = UserStatus.valueOf(statusStr.toUpperCase());
            user.setStatus(status);
            userRepository.save(user);

            // Audit Log
            User admin = userRepository.findById(adminId).orElse(null);
            String details = String.format("Changed status of user %s (ID: %d) from %s to %s",
                    user.getUsername(), user.getId(), oldStatus, status);
            auditLogService.log(admin, "UPDATE_STATUS", "USER", String.valueOf(user.getId()), details, ip, ua);

            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    public List<AuditLog> getAllAuditLogs() {
        return auditLogService.getUserLogs(null);
    }
}
