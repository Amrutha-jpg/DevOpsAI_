package com.devopsai.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_pipeline_runs")
public class TestPipelineRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "commit_sha", nullable = false)
    private String commitSha;

    @Column(nullable = false)
    private String branch = "main";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PipelineStatus status = PipelineStatus.PENDING;

    @Column(name = "stage_results_json", columnDefinition = "TEXT")
    private String stageResultsJson;

    @Column(name = "total_tests_count")
    private int totalTestsCount;

    @Column(name = "passed_count")
    private int passedCount;

    @Column(name = "failed_count")
    private int failedCount;

    @Column(name = "coverage_percent")
    private double coveragePercent;

    @Column(name = "duration_ms")
    private long durationMs;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public TestPipelineRun() {
    }

    public TestPipelineRun(Long projectId, String commitSha, String branch) {
        this.projectId = projectId;
        this.commitSha = commitSha;
        this.branch = branch != null ? branch : "main";
        this.status = PipelineStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getCommitSha() {
        return commitSha;
    }

    public void setCommitSha(String commitSha) {
        this.commitSha = commitSha;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public PipelineStatus getStatus() {
        return status;
    }

    public void setStatus(PipelineStatus status) {
        this.status = status;
    }

    public String getStageResultsJson() {
        return stageResultsJson;
    }

    public void setStageResultsJson(String stageResultsJson) {
        this.stageResultsJson = stageResultsJson;
    }

    public int getTotalTestsCount() {
        return totalTestsCount;
    }

    public void setTotalTestsCount(int totalTestsCount) {
        this.totalTestsCount = totalTestsCount;
    }

    public int getPassedCount() {
        return passedCount;
    }

    public void setPassedCount(int passedCount) {
        this.passedCount = passedCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public double getCoveragePercent() {
        return coveragePercent;
    }

    public void setCoveragePercent(double coveragePercent) {
        this.coveragePercent = coveragePercent;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
