package com.portfolio.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Entity
@Table(name = "file_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "config_id")
    private AppConfig uploadConfig;

    @Transient
    @JsonProperty("path") // Keep "path" in JSON for frontend compatibility
    private String url;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize; // size in bytes

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;
}
