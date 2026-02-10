package com.portfolio.backend.service;

import com.portfolio.backend.entity.Project;
import com.portfolio.backend.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PortfolioService portfolioService;

    private Long getCurrentUserId() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if (principal instanceof com.portfolio.backend.security.UserDetailsImpl) {
            return ((com.portfolio.backend.security.UserDetailsImpl) principal).getId();
        }
        return null;
    }

    public List<Project> getAllProjects(boolean includeHidden) {
        Long userId = getCurrentUserId();
        if (userId == null)
            return List.of();

        List<Project> projects;
        if (includeHidden) {
            projects = projectRepository.findByUserIdOrderByOrderAsc(userId);
        } else {
            projects = projectRepository.findByUserIdAndIsHiddenFalseOrderByOrderAsc(userId);
        }
        portfolioService.resolveProjectUrls(projects);
        return projects;
    }

    public Optional<Project> getProjectById(Long id) {
        Optional<Project> project = projectRepository.findById(id);
        project.ifPresent(p -> portfolioService.resolveProjectUrls(java.util.Collections.singletonList(p)));
        return project;
    }

    public Project createProject(Project project) {
        Long userId = getCurrentUserId();
        if (userId != null) {
            com.portfolio.backend.entity.User user = new com.portfolio.backend.entity.User();
            user.setId(userId);
            project.setUser(user);
        }
        return projectRepository.save(project);
    }

    public Project updateProject(Long id, Project projectDetails) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

        // Ensure the project belongs to the current user
        Long userId = getCurrentUserId();
        if (project.getUser() == null || !project.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to project");
        }

        project.setTitle(projectDetails.getTitle());
        project.setDescription(projectDetails.getDescription());
        project.setTechStack(projectDetails.getTechStack());
        project.setImageUrl(projectDetails.getImageUrl());
        project.setLiveLink(projectDetails.getLiveLink());
        project.setGithubLink(projectDetails.getGithubLink());
        project.setCategory(projectDetails.getCategory());
        project.setOrder(projectDetails.getOrder());
        project.setHidden(projectDetails.isHidden());

        return projectRepository.save(project);
    }

    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

        Long userId = getCurrentUserId();
        if (project.getUser() != null && project.getUser().getId().equals(userId)) {
            projectRepository.deleteById(id);
        }
    }
}
