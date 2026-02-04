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

    public List<Project> getAllProjects(boolean isAdmin) {
        if (isAdmin) {
            return projectRepository.findAllByOrderByOrderAsc();
        }
        return projectRepository.findByIsHiddenFalseOrderByOrderAsc();
    }

    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }

    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    public Project updateProject(Long id, Project projectDetails) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        
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
        projectRepository.deleteById(id);
    }
}
