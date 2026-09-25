package com.devopsai.backend.controller;

import com.devopsai.backend.dto.AnalyzeSnippetRequest;
import com.devopsai.backend.dto.CodeReviewDto;
import com.devopsai.backend.service.AiCodeReviewService;
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

    public AiCodeReviewController(AiCodeReviewService aiCodeReviewService) {
        this.aiCodeReviewService = aiCodeReviewService;
    }

    /**
     * Triggers asynchronous AI code review analysis for a pull request. Returns HTTP 202 Accepted.
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
