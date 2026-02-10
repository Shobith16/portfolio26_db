package com.portfolio.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "profile_views")
@Data
@NoArgsConstructor
public class ProfileView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_user_id")
    private User portfolioOwner;

    private String ipAddress;
    private String userAgent;
    private String deviceType;
    private String browser;
    private String os;
    private String city;
    private String country;
    private String referrerUrl;

    private Integer timeOnPage; // in seconds

    @CreationTimestamp
    private LocalDateTime viewedAt;
}
