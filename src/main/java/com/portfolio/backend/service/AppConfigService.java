package com.portfolio.backend.service;

import com.portfolio.backend.entity.AppConfig;
import com.portfolio.backend.repository.AppConfigRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Date;

@Service
public class AppConfigService {

    @Autowired
    private AppConfigRepository appConfigRepository;

    private static final String KEY_BASE_URL = "BASE_URL";
    private static final String KEY_UPLOAD_PATH = "PROFILE_UPLOAD_PATH";
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private static final String DEFAULT_UPLOAD_PATH = "/uploads/";

    @PostConstruct
    public void init() {
        // Initialize default configs if not present
        // TODO: Fix Lombok generation issues with AppConfig setters
        /*
        if (appConfigRepository.findByConfigKey(KEY_BASE_URL).isEmpty()) {
            AppConfig config = new AppConfig();
            config.setConfigKey(KEY_BASE_URL);
            config.setConfigValue(DEFAULT_BASE_URL);
            config.setDescription("Base URL of the backend server");
            config.setCategory("System");
            config.setDefaultValue(DEFAULT_BASE_URL);
            config.setIsEditable(true);
            config.setIsSensitive(false);
            config.setCreatedBy("System");
            appConfigRepository.save(config);
        }

        if (appConfigRepository.findByConfigKey(KEY_UPLOAD_PATH).isEmpty()) {
            AppConfig config = new AppConfig();
            config.setConfigKey(KEY_UPLOAD_PATH);
            config.setConfigValue(DEFAULT_UPLOAD_PATH);
            config.setDescription("Relative path for profile photo uploads");
            config.setCategory("System");
            config.setDefaultValue(DEFAULT_UPLOAD_PATH);
            config.setIsEditable(true);
            config.setCreatedBy("System");
            appConfigRepository.save(config);
        }
        */
    }

    public List<AppConfig> getAllConfigs() {
        return appConfigRepository.findAll();
    }

    public Optional<AppConfig> getConfig(String key) {
        return appConfigRepository.findByConfigKey(key);
    }

    public AppConfig createConfig(AppConfig config, String createdBy) {
        if (appConfigRepository.findByConfigKey(config.getConfigKey()).isPresent()) {
            throw new RuntimeException("Config already exists: " + config.getConfigKey());
        }
        config.setCreatedBy(createdBy);
        config.setCreatedAt(new Date());
        return appConfigRepository.save(config);
    }

    public AppConfig updateConfig(String key, String value, String updatedBy) {
        AppConfig config = appConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new RuntimeException("Config not found: " + key));

        if (Boolean.FALSE.equals(config.getIsEditable())) {
            throw new RuntimeException("Config is not editable: " + key);
        }

        config.setConfigValue(value);
        config.setUpdatedBy(updatedBy);
        config.setUpdatedAt(new Date());
        return appConfigRepository.save(config);
    }

    public void deleteConfig(String key) {
        AppConfig config = appConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new RuntimeException("Config not found: " + key));

        if (Boolean.FALSE.equals(config.getIsEditable())) {
            throw new RuntimeException("System config cannot be deleted: " + key);
        }

        appConfigRepository.delete(config);
    }

    public String getBaseUrl() {
        return appConfigRepository.findByConfigKey(KEY_BASE_URL)
                .map(AppConfig::getConfigValue)
                .orElse(DEFAULT_BASE_URL);
    }

    public String getUploadPath() {
        return appConfigRepository.findByConfigKey(KEY_UPLOAD_PATH)
                .map(AppConfig::getConfigValue)
                .orElse(DEFAULT_UPLOAD_PATH);
    }

    public AppConfig getUploadConfig() {
        return appConfigRepository.findByConfigKey(KEY_UPLOAD_PATH)
                .orElse(null);
    }
}
