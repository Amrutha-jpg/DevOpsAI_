package com.devopsai.backend.controller;

import com.devopsai.backend.dto.TestPipelineRunDto;
import com.devopsai.backend.dto.TriggerPipelineRequest;
import com.devopsai.backend.service.TestPipelineService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/testing")
public class TestPipelineController {

    private final TestPipelineService testPipelineService;

    public TestPipelineController(TestPipelineService testPipelineService) {
        this.testPipelineService = testPipelineService;
    }

    @PostMapping("/projects/{projectId}/trigger")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'DEVELOPER')")
    public ResponseEntity<TestPipelineRunDto> triggerPipeline(@PathVariable Long projectId,
                                                               @RequestBody(required = false) TriggerPipelineRequest request) {
        TestPipelineRunDto run = testPipelineService.triggerPipeline(projectId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(run);
    }

    @GetMapping("/projects/{projectId}/runs")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'VIEWER')")
    public ResponseEntity<List<TestPipelineRunDto>> getPipelineRunsForProject(@PathVariable Long projectId) {
        List<TestPipelineRunDto> runs = testPipelineService.getPipelineRunsForProject(projectId);
        return ResponseEntity.ok(runs);
    }

    @GetMapping("/runs/{runId}")
    public ResponseEntity<TestPipelineRunDto> getPipelineRun(@PathVariable Long runId) {
        TestPipelineRunDto run = testPipelineService.getPipelineRun(runId);
        return ResponseEntity.ok(run);
    }

    @GetMapping("/github-workflow")
    public ResponseEntity<String> getGitHubWorkflowYaml() {
        String yaml = testPipelineService.getGitHubWorkflowYaml();
        return ResponseEntity.ok(yaml);
    }
}
