package com.portfolio.backend.controller;

import com.portfolio.backend.entity.AppConfig;
import com.portfolio.backend.service.AppConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/configs")
public class ConfigController {

    @Autowired
    private AppConfigService appConfigService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<AppConfig> getAllConfigs() {
        return appConfigService.getAllConfigs();
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createConfig(@RequestBody AppConfig config) {
        try {
            AppConfig createdConfig = appConfigService.createConfig(config, "SuperAdmin");
            return ResponseEntity.ok(createdConfig);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateConfig(@PathVariable String key, @RequestBody Map<String, String> payload) {
        String value = payload.get("value");
        if (value == null) {
            return ResponseEntity.badRequest().body("Value is required");
        }

        // TODO: Get real username from security context if needed
        String updatedBy = "SuperAdmin";

        try {
            AppConfig updatedConfig = appConfigService.updateConfig(key, value, updatedBy);
            return ResponseEntity.ok(updatedConfig);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{key}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteConfig(@PathVariable String key) {
        try {
            appConfigService.deleteConfig(key);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
