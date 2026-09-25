package com.devopsai.backend.controller;

import com.devopsai.backend.dto.*;
import com.devopsai.backend.entity.SyncStatus;
import com.devopsai.backend.service.GithubIntegrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GithubIntegrationController {

    private final GithubIntegrationService githubIntegrationService;

    public GithubIntegrationController(GithubIntegrationService githubIntegrationService) {
        this.githubIntegrationService = githubIntegrationService;
    }

    @PostMapping("/projects/{projectId}/github/connect")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'PROJECT_MANAGER')")
    public ResponseEntity<GithubRepoDto> connectRepository(@PathVariable Long projectId,
                                                           @Valid @RequestBody ConnectGithubRepoRequest request) {
        GithubRepoDto connected = githubIntegrationService.connectRepository(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(connected);
    }

    @GetMapping("/projects/{projectId}/github/repo")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'VIEWER')")
    public ResponseEntity<GithubRepoDto> getRepoForProject(@PathVariable Long projectId) {
        GithubRepoDto repo = githubIntegrationService.getRepoForProject(projectId);
        return ResponseEntity.ok(repo);
    }

    /**
     * Triggers asynchronous repository synchronization. Returns HTTP 202 Accepted immediately.
     */
    @PostMapping("/projects/{projectId}/github/sync")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'DEVELOPER')")
    public ResponseEntity<SyncStatusResponse> syncRepository(@PathVariable Long projectId) {
        GithubRepoDto repo = githubIntegrationService.getRepoForProject(projectId);

        // Trigger background @Async sync worker
        githubIntegrationService.syncRepositoryAsync(repo.getId());

        SyncStatusResponse response = new SyncStatusResponse(
            repo.getId(),
            SyncStatus.IN_PROGRESS,
            "Asynchronous GitHub repository sync task launched successfully."
        );

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/projects/{projectId}/github/commits")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'VIEWER')")
    public ResponseEntity<List<CommitDto>> getCommits(@PathVariable Long projectId) {
        List<CommitDto> commits = githubIntegrationService.getCommits(projectId);
        return ResponseEntity.ok(commits);
    }

    @GetMapping("/projects/{projectId}/github/pulls")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'VIEWER')")
    public ResponseEntity<List<PullRequestDto>> getPullRequests(@PathVariable Long projectId) {
        List<PullRequestDto> pulls = githubIntegrationService.getPullRequests(projectId);
        return ResponseEntity.ok(pulls);
    }

    @GetMapping("/pulls/{pullRequestId}/files")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChangedFileDto>> getChangedFiles(@PathVariable Long pullRequestId) {
        List<ChangedFileDto> files = githubIntegrationService.getChangedFiles(pullRequestId);
        return ResponseEntity.ok(files);
    }
}
