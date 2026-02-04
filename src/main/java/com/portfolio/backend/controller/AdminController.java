package com.portfolio.backend.controller;

import com.portfolio.backend.entity.*;
import com.portfolio.backend.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class AdminController {

    @Autowired
    private PortfolioService portfolioService;

    // --- Public Endpoints (Skills, Experiences) ---
    @GetMapping("/public/skills")
    public List<Skill> getPublicSkills() {
        return portfolioService.getAllSkills(); // Add filtering if needed
    }

    @GetMapping("/public/experiences")
    public List<Experience> getPublicExperiences() {
        return portfolioService.getAllExperiences();
    }

    @PostMapping("/public/messages")
    public Message contactMessage(@RequestBody Message message) {
        return portfolioService.saveMessage(message);
    }

    @GetMapping("/public/profile")
    public Profile getPublicProfile() {
        return portfolioService.getProfile();
    }

    @GetMapping("/public/contact")
    public ContactInfo getPublicContact() {
        return portfolioService.getContactInfo();
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
}
