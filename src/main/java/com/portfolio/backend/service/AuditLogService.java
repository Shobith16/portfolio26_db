package com.portfolio.backend.service;

import com.portfolio.backend.entity.AuditLog;
import com.portfolio.backend.entity.User;
import com.portfolio.backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(User user, String action, String resource, String resourceId, String details, String ip,
            String ua) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setActionType(action);
        log.setResourceType(resource);
        log.setResourceId(resourceId);
        log.setDetails(details);
        log.setIpAddress(ip);
        log.setUserAgent(ua);
        auditLogRepository.save(log);
    }

    public List<AuditLog> getUserLogs(Long userId) {
        if (userId == null) {
            return auditLogRepository.findAllByOrderByTimestampDesc();
        }
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }
}
