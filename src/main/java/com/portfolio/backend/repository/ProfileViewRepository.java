package com.portfolio.backend.repository;

import com.portfolio.backend.entity.ProfileView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProfileViewRepository extends JpaRepository<ProfileView, Long> {
    List<ProfileView> findByPortfolioOwnerIdOrderByViewedAtDesc(Long userId);

    long countByPortfolioOwnerId(Long userId);

    List<ProfileView> findByPortfolioOwnerIdAndViewedAtAfter(Long userId, LocalDateTime after);
}
