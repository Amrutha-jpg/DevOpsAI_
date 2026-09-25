package com.devopsai.backend.controller;

import com.devopsai.backend.dto.DeploymentStatusDto;
import com.devopsai.backend.dto.PipelineRunDto;
import com.devopsai.backend.dto.TriggerPipelineRequest;
import com.devopsai.backend.service.CloudDeploymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deployment")
@CrossOrigin(origins = "*")
public class CloudDeploymentController {

    @Autowired
    private CloudDeploymentService cloudDeploymentService;

    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'DEVELOPER', 'VIEWER')")
    public ResponseEntity<DeploymentStatusDto> getDeploymentStatus() {
        DeploymentStatusDto status = cloudDeploymentService.getDeploymentStatus();
        return ResponseEntity.ok(status);
    }

    @PostMapping("/pipeline/trigger")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'DEVELOPER')")
    public ResponseEntity<PipelineRunDto> triggerDeploymentPipeline(
            @RequestBody(required = false) TriggerPipelineRequest request,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        PipelineRunDto initiatedRun = cloudDeploymentService.initiateDeploymentPipeline(request, username);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(initiatedRun);
    }

    @GetMapping("/pipeline/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'DEVELOPER', 'VIEWER')")
    public ResponseEntity<List<PipelineRunDto>> getPipelineHistory() {
        List<PipelineRunDto> history = cloudDeploymentService.getPipelineHistory();
        return ResponseEntity.ok(history);
    }
}
