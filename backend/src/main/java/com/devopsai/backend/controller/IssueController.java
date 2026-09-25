package com.devopsai.backend.controller;

import com.devopsai.backend.dto.IssueDto;
import com.devopsai.backend.dto.IssueRequest;
import com.devopsai.backend.entity.IssueStatus;
import com.devopsai.backend.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @PostMapping("/projects/{projectId}/issues")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'DEVELOPER')")
    public ResponseEntity<IssueDto> createIssue(@PathVariable Long projectId,
                                                @Valid @RequestBody IssueRequest request,
                                                Authentication authentication) {
        IssueDto issue = issueService.createIssue(projectId, request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(issue);
    }

    @GetMapping("/projects/{projectId}/issues")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'VIEWER')")
    public ResponseEntity<List<IssueDto>> getIssuesForProject(@PathVariable Long projectId,
                                                              @RequestParam(required = false) Long sprintId,
                                                              @RequestParam(required = false) Long epicId,
                                                              @RequestParam(required = false) IssueStatus status) {
        List<IssueDto> issues = issueService.getIssuesForProject(projectId, sprintId, epicId, status);
        return ResponseEntity.ok(issues);
    }

    @GetMapping("/projects/{projectId}/issues/prioritized")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'VIEWER')")
    public ResponseEntity<List<IssueDto>> getPrioritizedBacklog(@PathVariable Long projectId) {
        List<IssueDto> prioritizedIssues = issueService.getPrioritizedBacklog(projectId);
        return ResponseEntity.ok(prioritizedIssues);
    }

    @PutMapping("/issues/{issueId}/status")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasIssueProjectPermission(#issueId, authentication, 'DEVELOPER')")
    public ResponseEntity<IssueDto> updateIssueStatus(@PathVariable Long issueId,
                                                      @RequestParam IssueStatus status) {
        IssueDto updatedIssue = issueService.updateIssueStatus(issueId, status);
        return ResponseEntity.ok(updatedIssue);
    }

    @PutMapping("/issues/{issueId}")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasIssueProjectPermission(#issueId, authentication, 'DEVELOPER')")
    public ResponseEntity<IssueDto> updateIssue(@PathVariable Long issueId,
                                                @Valid @RequestBody IssueRequest request) {
        IssueDto updatedIssue = issueService.updateIssue(issueId, request);
        return ResponseEntity.ok(updatedIssue);
    }

    @DeleteMapping("/issues/{issueId}")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasIssueProjectPermission(#issueId, authentication, 'PROJECT_MANAGER')")
    public ResponseEntity<Void> deleteIssue(@PathVariable Long issueId) {
        issueService.deleteIssue(issueId);
        return ResponseEntity.noContent().build();
    }
}
