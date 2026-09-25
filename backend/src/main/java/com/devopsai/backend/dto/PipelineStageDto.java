package com.devopsai.backend.dto;

import java.util.List;

public class PipelineStageDto {

    private String stageName; // BUILD, UNIT_TESTS, INTEGRATION_TESTS, API_CONTRACT_TESTS, AI_SECURITY_SCAN
    private String status; // PASSED, FAILED, RUNNING, PENDING
    private long durationMs;
    private List<String> logs;

    public PipelineStageDto() {
    }

    public PipelineStageDto(String stageName, String status, long durationMs, List<String> logs) {
        this.stageName = stageName;
        this.status = status;
        this.durationMs = durationMs;
        this.logs = logs;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }
}
