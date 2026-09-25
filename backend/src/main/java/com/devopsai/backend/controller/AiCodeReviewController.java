package com.devopsai.backend.controller;

import com.devopsai.backend.dto.AnalyzeSnippetRequest;
import com.devopsai.backend.dto.CodeReviewDto;
import com.devopsai.backend.dto.PullRequestDto;
import com.devopsai.backend.service.AiCodeReviewService;
import com.devopsai.backend.service.GithubIntegrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AiCodeReviewController {

    private final AiCodeReviewService aiCodeReviewService;
    private final GithubIntegrationService githubIntegrationService;

    public AiCodeReviewController(AiCodeReviewService aiCodeReviewService,
                                  GithubIntegrationService githubIntegrationService) {
        this.aiCodeReviewService = aiCodeReviewService;
        this.githubIntegrationService = githubIntegrationService;
    }

    /**
     * Scoped PR Listing for a project.
     */
    @GetMapping("/reviews/projects/{projectId}/pulls")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'VIEWER')")
    public ResponseEntity<List<PullRequestDto>> getPullRequestsForReview(@PathVariable Long projectId) {
        List<PullRequestDto> pulls = githubIntegrationService.getPullRequests(projectId);
        return ResponseEntity.ok(pulls);
    }

    /**
     * Triggers asynchronous AI code review analysis for a pull request by PR number.
     */
    @PostMapping("/reviews/projects/{projectId}/pr/{prNumber}")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'DEVELOPER')")
    public ResponseEntity<CodeReviewDto> triggerReviewByPrNumber(@PathVariable Long projectId,
                                                                  @PathVariable Integer prNumber) {
        CodeReviewDto reviewDto = aiCodeReviewService.initiatePullRequestReviewByNumber(projectId, prNumber);
        aiCodeReviewService.analyzePullRequestAsync(reviewDto.getId(), reviewDto.getPullRequestId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(reviewDto);
    }

    /**
     * Triggers asynchronous AI code review analysis for a pull request by PR database ID.
     */
    @PostMapping("/projects/{projectId}/reviews/analyze-pr/{pullRequestId}")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'DEVELOPER')")
    public ResponseEntity<CodeReviewDto> triggerPullRequestReview(@PathVariable Long projectId,
                                                                   @PathVariable Long pullRequestId) {
        CodeReviewDto reviewDto = aiCodeReviewService.initiatePullRequestReview(projectId, pullRequestId);
        aiCodeReviewService.analyzePullRequestAsync(reviewDto.getId(), pullRequestId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(reviewDto);
    }

    /**
     * Retrieves the latest review report for a project.
     */
    @GetMapping("/reviews/projects/{projectId}/latest")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'VIEWER')")
    public ResponseEntity<CodeReviewDto> getLatestReviewForProject(@PathVariable Long projectId) {
        CodeReviewDto dto = aiCodeReviewService.getLatestReviewForProject(projectId);
        if (dto == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dto);
    }

    /**
     * Analyzes an ad-hoc code snippet submitted by a developer.
     */
    @PostMapping("/reviews/analyze-snippet")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CodeReviewDto> analyzeSnippet(@Valid @RequestBody AnalyzeSnippetRequest request) {
        CodeReviewDto result = aiCodeReviewService.analyzeSnippet(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CodeReviewDto> getReview(@PathVariable Long reviewId) {
        CodeReviewDto dto = aiCodeReviewService.getReview(reviewId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/projects/{projectId}/reviews")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'VIEWER')")
    public ResponseEntity<List<CodeReviewDto>> getReviewsForProject(@PathVariable Long projectId) {
        List<CodeReviewDto> reviews = aiCodeReviewService.getReviewsForProject(projectId);
        return ResponseEntity.ok(reviews);
    }
}
