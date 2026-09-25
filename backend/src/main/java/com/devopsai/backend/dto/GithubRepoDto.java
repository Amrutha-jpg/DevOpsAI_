package com.devopsai.backend.dto;

import com.devopsai.backend.entity.GithubRepository;
import com.devopsai.backend.entity.SyncStatus;

import java.time.LocalDateTime;

public class GithubRepoDto {

    private Long id;
    private String repoName;
    private String ownerName;
    private Long githubRepoId;
    private String htmlUrl;
    private String defaultBranch;
    private String maskedAccessToken;
    private SyncStatus syncStatus;
    private LocalDateTime lastSyncedAt;
    private Long projectId;

    public GithubRepoDto() {
    }

    public GithubRepoDto(GithubRepository repo) {
        this.id = repo.getId();
        this.repoName = repo.getRepoName();
        this.ownerName = repo.getOwnerName();
        this.githubRepoId = repo.getGithubRepoId();
        this.htmlUrl = repo.getHtmlUrl();
        this.defaultBranch = repo.getDefaultBranch();
        this.maskedAccessToken = maskToken(repo.getAccessToken());
        this.syncStatus = repo.getSyncStatus();
        this.lastSyncedAt = repo.getLastSyncedAt();
        this.projectId = repo.getProject() != null ? repo.getProject().getId() : null;
    }

    public static String maskToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        if (token.length() <= 8) {
            return "********";
        }
        String prefix = token.substring(0, 4);
        String suffix = token.substring(token.length() - 4);
        return prefix + "*".repeat(token.length() - 8) + suffix;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Long getGithubRepoId() {
        return githubRepoId;
    }

    public void setGithubRepoId(Long githubRepoId) {
        this.githubRepoId = githubRepoId;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    public String getMaskedAccessToken() {
        return maskedAccessToken;
    }

    public void setMaskedAccessToken(String maskedAccessToken) {
        this.maskedAccessToken = maskedAccessToken;
    }

    public SyncStatus getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(SyncStatus syncStatus) {
        this.syncStatus = syncStatus;
    }

    public LocalDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(LocalDateTime lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
