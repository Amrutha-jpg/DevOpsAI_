package com.devopsai.backend.dto;

import com.devopsai.backend.entity.SyncStatus;

import java.time.LocalDateTime;

public class SyncStatusResponse {

    private Long repoId;
    private SyncStatus syncStatus;
    private String message;
    private LocalDateTime timestamp;

    public SyncStatusResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public SyncStatusResponse(Long repoId, SyncStatus syncStatus, String message) {
        this.repoId = repoId;
        this.syncStatus = syncStatus;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public Long getRepoId() {
        return repoId;
    }

    public void setRepoId(Long repoId) {
        this.repoId = repoId;
    }

    public SyncStatus getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(SyncStatus syncStatus) {
        this.syncStatus = syncStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
