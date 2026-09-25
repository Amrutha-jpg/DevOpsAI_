package com.devopsai.backend.dto;

import com.devopsai.backend.entity.CodeReview;
import com.devopsai.backend.entity.ReviewStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CodeReviewDto {

    private Long id;
    private ReviewStatus status;
    private double qualityScore;
    private String summary;
    private Long projectId;
    private Integer prNumber;
    private String prTitle;
    private List<CodeReviewFindingDto> findings;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CodeReviewDto() {
    }

    public CodeReviewDto(CodeReview review) {
        this.id = review.getId();
        this.status = review.getStatus();
        this.qualityScore = review.getQualityScore();
        this.summary = review.getSummary();
        this.projectId = review.getProject() != null ? review.getProject().getId() : null;
        if (review.getPullRequest() != null) {
            this.prNumber = review.getPullRequest().getNumber();
            this.prTitle = review.getPullRequest().getTitle();
        }
        this.findings = review.getFindings() != null
            ? review.getFindings().stream().map(CodeReviewFindingDto::new).collect(Collectors.toList())
            : List.of();
        this.createdAt = review.getCreatedAt();
        this.updatedAt = review.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }

    public double getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(double qualityScore) {
        this.qualityScore = qualityScore;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Integer getPrNumber() {
        return prNumber;
    }

    public void setPrNumber(Integer prNumber) {
        this.prNumber = prNumber;
    }

    public String getPrTitle() {
        return prTitle;
    }

    public void setPrTitle(String prTitle) {
        this.prTitle = prTitle;
    }

    public List<CodeReviewFindingDto> getFindings() {
        return findings;
    }

    public void setFindings(List<CodeReviewFindingDto> findings) {
        this.findings = findings;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
