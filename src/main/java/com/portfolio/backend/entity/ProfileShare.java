package com.portfolio.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "profile_shares")
@Data
@NoArgsConstructor
public class ProfileShare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_user_id")
    private User portfolioOwner;

    private String platform; // e.g., 'WhatsApp', 'Twitter', 'Copy Link'

    @CreationTimestamp
    private LocalDateTime sharedAt;
}
