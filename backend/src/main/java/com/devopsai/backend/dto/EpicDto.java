package com.devopsai.backend.dto;

import com.devopsai.backend.entity.Epic;

import java.time.LocalDateTime;

public class EpicDto {

    private Long id;
    private String name;
    private String description;
    private Long projectId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EpicDto() {
    }

    public EpicDto(Epic epic) {
        this.id = epic.getId();
        this.name = epic.getName();
        this.description = epic.getDescription();
        this.projectId = epic.getProject() != null ? epic.getProject().getId() : null;
        this.createdAt = epic.getCreatedAt();
        this.updatedAt = epic.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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
