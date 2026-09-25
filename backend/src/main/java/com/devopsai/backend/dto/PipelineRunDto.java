package com.devopsai.backend.dto;

import java.time.LocalDateTime;

public class PipelineRunDto {

    private String id;
    private String pipelineName;
    private String commitSha;
    private String targetEnvironment; // STAGING, PRODUCTION
    private String status; // PENDING, BUILDING, TESTING, SECURITY_SCAN, DEPLOYED, FAILED
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long durationSeconds;
    private String triggeredBy;

    public PipelineRunDto() {}

    public PipelineRunDto(String id, String pipelineName, String commitSha, String targetEnvironment,
                          String status, LocalDateTime startedAt, LocalDateTime completedAt,
                          Long durationSeconds, String triggeredBy) {
        this.id = id;
        this.pipelineName = pipelineName;
        this.commitSha = commitSha;
        this.targetEnvironment = targetEnvironment;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.durationSeconds = durationSeconds;
        this.triggeredBy = triggeredBy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }

    public String getCommitSha() {
        return commitSha;
    }

    public void setCommitSha(String commitSha) {
        this.commitSha = commitSha;
    }

    public String getTargetEnvironment() {
        return targetEnvironment;
    }

    public void setTargetEnvironment(String targetEnvironment) {
        this.targetEnvironment = targetEnvironment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }
}
