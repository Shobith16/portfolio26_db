package com.portfolio.backend.service;

import com.portfolio.backend.entity.*;
import com.portfolio.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortfolioService {

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

    // --- Skills ---
    public List<Skill> getAllSkills() {
        return skillRepository.findAllByOrderByOrderAsc();
    }

    public Skill saveSkill(Skill skill) {
        return skillRepository.save(skill);
    }

    public void deleteSkill(Long id) {
        skillRepository.deleteById(id);
    }

    // --- Experiences ---
    public List<Experience> getAllExperiences() {
        return experienceRepository.findAllByOrderByOrderAsc();
    }

    public Experience saveExperience(Experience exp) {
        return experienceRepository.save(exp);
    }

    public void deleteExperience(Long id) {
        experienceRepository.deleteById(id);
    }

    // --- Messages ---
    public List<Message> getAllMessages() {
        return messageRepository.findAllByOrderByCreatedAtDesc();
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

    // --- Profile ---
    public Profile getProfile() {
        List<Profile> profiles = profileRepository.findAll();
        return profiles.isEmpty() ? null : profiles.get(0);
    }

    public Profile updateProfile(Profile profile) {
        return profileRepository.save(profile);
    }

    // --- Contact Info ---
    public ContactInfo getContactInfo() {
        List<ContactInfo> contacts = contactInfoRepository.findAll();
        return contacts.isEmpty() ? null : contacts.get(0);
    }

    public ContactInfo updateContactInfo(ContactInfo contactInfo) {
        return contactInfoRepository.save(contactInfo);
    }
}
