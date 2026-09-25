package com.devopsai.backend.dto;

import java.time.LocalDateTime;

public class CodebaseIndexStatusDto {

    private Long projectId;
    private long totalChunks;
    private long indexedFilesCount;
    private LocalDateTime lastIndexedAt;
    private boolean isIndexing;

    public CodebaseIndexStatusDto() {
    }

    public CodebaseIndexStatusDto(Long projectId, long totalChunks, long indexedFilesCount, LocalDateTime lastIndexedAt, boolean isIndexing) {
        this.projectId = projectId;
        this.totalChunks = totalChunks;
        this.indexedFilesCount = indexedFilesCount;
        this.lastIndexedAt = lastIndexedAt;
        this.isIndexing = isIndexing;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public long getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(long totalChunks) {
        this.totalChunks = totalChunks;
    }

    public long getIndexedFilesCount() {
        return indexedFilesCount;
    }

    public void setIndexedFilesCount(long indexedFilesCount) {
        this.indexedFilesCount = indexedFilesCount;
    }

    public LocalDateTime getLastIndexedAt() {
        return lastIndexedAt;
    }

    public void setLastIndexedAt(LocalDateTime lastIndexedAt) {
        this.lastIndexedAt = lastIndexedAt;
    }

    public boolean isIndexing() {
        return isIndexing;
    }

    public void setIndexing(boolean indexing) {
        isIndexing = indexing;
    }
}
