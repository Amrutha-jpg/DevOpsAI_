package com.devopsai.backend.controller;

import com.devopsai.backend.dto.SprintDto;
import com.devopsai.backend.dto.SprintRequest;
import com.devopsai.backend.entity.SprintStatus;
import com.devopsai.backend.service.SprintService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SprintController {

    private final SprintService sprintService;

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @PostMapping("/projects/{projectId}/sprints")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'PROJECT_MANAGER')")
    public ResponseEntity<SprintDto> createSprint(@PathVariable Long projectId, @Valid @RequestBody SprintRequest request) {
        SprintDto sprint = sprintService.createSprint(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(sprint);
    }

    @GetMapping("/projects/{projectId}/sprints")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'VIEWER')")
    public ResponseEntity<List<SprintDto>> getSprintsForProject(@PathVariable Long projectId) {
        List<SprintDto> sprints = sprintService.getSprintsForProject(projectId);
        return ResponseEntity.ok(sprints);
    }

    @PutMapping("/sprints/{sprintId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SprintDto> updateSprintStatus(@PathVariable Long sprintId, @RequestParam SprintStatus status) {
        SprintDto sprint = sprintService.updateSprintStatus(sprintId, status);
        return ResponseEntity.ok(sprint);
    }
}
