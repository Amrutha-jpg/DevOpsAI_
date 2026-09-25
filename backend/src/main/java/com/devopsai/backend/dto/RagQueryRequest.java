package com.devopsai.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RagQueryRequest {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotBlank(message = "Query cannot be blank")
    private String query;

    private Integer maxSources = 5;

    private Long repositoryId;

    public RagQueryRequest() {
    }

    public RagQueryRequest(Long projectId, String query, Integer maxSources) {
        this.projectId = projectId;
        this.query = query;
        this.maxSources = maxSources != null ? maxSources : 5;
    }

    public RagQueryRequest(Long projectId, Long repositoryId, String query, Integer maxSources) {
        this.projectId = projectId;
        this.repositoryId = repositoryId;
        this.query = query;
        this.maxSources = maxSources != null ? maxSources : 5;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(Long repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Integer getMaxSources() {
        return maxSources;
    }

    public void setMaxSources(Integer maxSources) {
        this.maxSources = maxSources;
    }
}
