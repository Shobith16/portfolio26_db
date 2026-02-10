package com.portfolio.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user; // Person who performed the action

    private String actionType; // LOGIN, UPDATE_PROFILE, etc.
    private String resourceType; // PROFILE, PROJECT, etc.
    private String resourceId;

    @Column(columnDefinition = "TEXT")
    private String details;

    private String ipAddress;
    private String userAgent;

    @CreationTimestamp
    private LocalDateTime timestamp;
}
