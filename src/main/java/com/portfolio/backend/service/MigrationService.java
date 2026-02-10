package com.portfolio.backend.service;

import com.portfolio.backend.entity.*;
import com.portfolio.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MigrationService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ProjectRepository projectRepository;
    private final ExperienceRepository experienceRepository;
    private final SkillRepository skillRepository;
    private final ContactInfoRepository contactInfoRepository;
    private final MessageRepository messageRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void migrate() {
        log.info("Starting data migration check...");

        // 1. Ensure default Super Admin exists
        User admin = userRepository.findByUsername("admin")
                .orElseGet(() -> userRepository.findByEmail("admin@portfolio.com").orElse(null));

        if (admin == null) {
            log.info("Creating default super admin account...");
            User newUser = new User();
            newUser.setUsername("admin");
            newUser.setEmail("admin@portfolio.com");
            newUser.setPassword(passwordEncoder.encode("admin123")); // Default password
            newUser.setRole("ROLE_SUPER_ADMIN");
            newUser.setStatus(UserStatus.ACTIVE);
            newUser.setFullName("Super Admin");
            admin = userRepository.save(newUser);
        }

        // 2. Link orphaned data to the admin user
        linkProfilesToUser(admin);
        linkProjectsToUser(admin);
        linkExperiencesToUser(admin);
        linkSkillsToUser(admin);
        linkContactInfoToUser(admin);
        linkMessagesToUser(admin);

        log.info("Data migration check completed.");
    }

    private void linkProfilesToUser(User user) {
        List<Profile> profiles = profileRepository.findAll();
        for (Profile p : profiles) {
            if (p.getUser() == null) {
                p.setUser(user);
                profileRepository.save(p);
            }
        }
    }

    private void linkProjectsToUser(User user) {
        List<Project> projects = projectRepository.findAll();
        for (Project p : projects) {
            if (p.getUser() == null) {
                p.setUser(user);
                projectRepository.save(p);
            }
        }
    }

    private void linkExperiencesToUser(User user) {
        List<Experience> experiences = experienceRepository.findAll();
        for (Experience e : experiences) {
            if (e.getUser() == null) {
                e.setUser(user);
                experienceRepository.save(e);
            }
        }
    }

    private void linkSkillsToUser(User user) {
        List<Skill> skills = skillRepository.findAll();
        for (Skill s : skills) {
            if (s.getUser() == null) {
                s.setUser(user);
                skillRepository.save(s);
            }
        }
    }

    private void linkContactInfoToUser(User user) {
        List<ContactInfo> contactInfos = contactInfoRepository.findAll();
        for (ContactInfo ci : contactInfos) {
            if (ci.getUser() == null) {
                ci.setUser(user);
                contactInfoRepository.save(ci);
            }
        }
    }

    private void linkMessagesToUser(User user) {
        List<Message> messages = messageRepository.findAll();
        for (Message m : messages) {
            if (m.getUser() == null) {
                m.setUser(user);
                messageRepository.save(m);
            }
        }
    }
}
