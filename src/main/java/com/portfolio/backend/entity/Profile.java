package com.portfolio.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(length = 150)
    private String tagline;

    @Column(length = 200)
    private String intro;

    @Column(length = 100)
    private String degree;

    @Column(length = 150)
    private String college;

    @Column(precision = 3, scale = 2)
    private BigDecimal cgpa;

    @Column(name = "about_summary", columnDefinition = "TEXT")
    private String aboutSummary;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "photo_id")
    private FileMetadata photo;

    @Column(name = "show_about")
    private Boolean showAbout = true;

    @Column(name = "show_skills")
    private Boolean showSkills = true;

    @Column(name = "show_experiences")
    private Boolean showExperiences = true;

    @Column(name = "show_projects")
    private Boolean showProjects = true;

    @Column(name = "show_contact")
    private Boolean showContact = true;

    // Granular field toggles
    @Column(name = "show_full_name")
    private Boolean showFullName = true;

    @Column(name = "show_tagline")
    private Boolean showTagline = true;

    @Column(name = "show_intro")
    private Boolean showIntro = true;

    @Column(name = "show_degree")
    private Boolean showDegree = true;

    @Column(name = "show_college")
    private Boolean showCollege = true;

    @Column(name = "show_cgpa")
    private Boolean showCgpa = true;

    @Column(name = "show_about_summary")
    private Boolean showAboutSummary = true;

    @Column(name = "show_photo")
    private Boolean showPhoto = true;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
}
