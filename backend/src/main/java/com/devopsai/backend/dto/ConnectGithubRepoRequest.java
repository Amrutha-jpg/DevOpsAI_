package com.devopsai.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class ConnectGithubRepoRequest {

    @NotBlank(message = "GitHub owner name is required")
    private String ownerName;

    @NotBlank(message = "GitHub repository name is required")
    private String repoName;

    private String defaultBranch = "main";
    private String accessToken; // Personal Access Token or OAuth Token

    public ConnectGithubRepoRequest() {
    }

    public ConnectGithubRepoRequest(String ownerName, String repoName, String defaultBranch, String accessToken) {
        this.ownerName = ownerName;
        this.repoName = repoName;
        this.defaultBranch = defaultBranch != null ? defaultBranch : "main";
        this.accessToken = accessToken;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
