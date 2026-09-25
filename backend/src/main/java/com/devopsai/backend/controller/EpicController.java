package com.devopsai.backend.controller;

import com.devopsai.backend.dto.EpicDto;
import com.devopsai.backend.dto.EpicRequest;
import com.devopsai.backend.service.EpicService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EpicController {

    private final EpicService epicService;

    public EpicController(EpicService epicService) {
        this.epicService = epicService;
    }

    @PostMapping("/projects/{projectId}/epics")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'PROJECT_MANAGER')")
    public ResponseEntity<EpicDto> createEpic(@PathVariable Long projectId, @Valid @RequestBody EpicRequest request) {
        EpicDto epic = epicService.createEpic(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(epic);
    }

    @GetMapping("/projects/{projectId}/epics")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'VIEWER')")
    public ResponseEntity<List<EpicDto>> getEpicsForProject(@PathVariable Long projectId) {
        List<EpicDto> epics = epicService.getEpicsForProject(projectId);
        return ResponseEntity.ok(epics);
    }

    @DeleteMapping("/epics/{epicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEpic(@PathVariable Long epicId) {
        epicService.deleteEpic(epicId);
        return ResponseEntity.noContent().build();
    }
}
