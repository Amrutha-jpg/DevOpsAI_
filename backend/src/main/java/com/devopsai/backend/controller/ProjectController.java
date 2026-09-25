package com.devopsai.backend.controller;

import com.devopsai.backend.dto.ProjectDto;
import com.devopsai.backend.dto.ProjectRequest;
import com.devopsai.backend.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProjectDto>> getProjects(Authentication authentication) {
        List<ProjectDto> projects = projectService.getProjectsForUser(authentication);
        return ResponseEntity.ok(projects);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public ResponseEntity<ProjectDto> createProject(@Valid @RequestBody ProjectRequest request, Authentication authentication) {
        ProjectDto project = projectService.createProject(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#id, authentication, 'PROJECT_MANAGER')")
    public ResponseEntity<ProjectDto> updateProject(@PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
        ProjectDto updatedProject = projectService.updateProject(id, request);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
