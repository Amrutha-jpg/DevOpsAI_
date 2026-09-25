package com.devopsai.backend.dto;

import com.devopsai.backend.entity.PipelineStatus;

import java.time.LocalDateTime;
import java.util.List;

public class TestPipelineRunDto {

    private Long id;
    private Long projectId;
    private String commitSha;
    private String branch;
    private PipelineStatus status;
    private List<PipelineStageDto> stages;
    private int totalTestsCount;
    private int passedCount;
    private int failedCount;
    private double coveragePercent;
    private long durationMs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TestPipelineRunDto() {
    }

    public TestPipelineRunDto(Long id, Long projectId, String commitSha, String branch, PipelineStatus status,
                              List<PipelineStageDto> stages, int totalTestsCount, int passedCount, int failedCount,
                              double coveragePercent, long durationMs, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.projectId = projectId;
        this.commitSha = commitSha;
        this.branch = branch;
        this.status = status;
        this.stages = stages;
        this.totalTestsCount = totalTestsCount;
        this.passedCount = passedCount;
        this.failedCount = failedCount;
        this.coveragePercent = coveragePercent;
        this.durationMs = durationMs;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public List<PipelineStageDto> getStages() {
        return stages;
    }

    public void setStages(List<PipelineStageDto> stages) {
        this.stages = stages;
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
