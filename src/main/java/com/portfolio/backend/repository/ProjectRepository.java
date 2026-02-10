package com.portfolio.backend.repository;

import com.portfolio.backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByIsHiddenFalseOrderByOrderAsc();

    List<Project> findAllByOrderByOrderAsc();

    List<Project> findByUserIdOrderByOrderAsc(Long userId);

    List<Project> findByUserIdAndIsHiddenFalseOrderByOrderAsc(Long userId);

    void deleteByUserId(Long userId);
}
