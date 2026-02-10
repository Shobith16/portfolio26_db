package com.portfolio.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "app_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id")
    private Long configId;

    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    @Column(name = "value_type", length = 20)
    private String valueType = "string"; // string, number, boolean, json

    @Column(length = 50)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_sensitive")
    private Boolean isSensitive = false;

    @Column(name = "is_editable")
    private Boolean isEditable = true;

    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private java.util.Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private java.util.Date updatedAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;
}
