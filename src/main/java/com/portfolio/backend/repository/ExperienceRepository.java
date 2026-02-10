package com.portfolio.backend.repository;

import com.portfolio.backend.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    List<Experience> findAllByOrderByOrderAsc();

    List<Experience> findByUserIdOrderByOrderAsc(Long userId);

    void deleteByUserId(Long userId);
}
