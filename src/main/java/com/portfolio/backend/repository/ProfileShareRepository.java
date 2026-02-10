package com.portfolio.backend.repository;

import com.portfolio.backend.entity.ProfileShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProfileShareRepository extends JpaRepository<ProfileShare, Long> {
    long countByPortfolioOwnerId(Long userId);

    List<ProfileShare> findByPortfolioOwnerIdAndSharedAtAfter(Long userId, LocalDateTime after);
}
