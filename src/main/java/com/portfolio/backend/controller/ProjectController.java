package com.portfolio.backend.controller;

import com.portfolio.backend.entity.Project;
import com.portfolio.backend.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
public class ProjectController {
    @Autowired
    private ProjectService projectService;

    // Authenticated user's projects for admin panel
    @GetMapping("/projects")
    @PreAuthorize("isAuthenticated()")
    public List<Project> getAllProjects() {
        return projectService.getAllProjects(true); // include hidden
    }

    @GetMapping("/projects/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/projects")
    @PreAuthorize("isAuthenticated()")
    public Project createProject(@RequestBody Project project) {
        return projectService.createProject(project);
    }

    @PutMapping("/projects/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @RequestBody Project projectDetails) {
        return ResponseEntity.ok(projectService.updateProject(id, projectDetails));
    }

    @DeleteMapping("/projects/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok().build();
    }
}
