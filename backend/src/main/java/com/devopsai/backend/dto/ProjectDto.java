package com.devopsai.backend.dto;

import com.devopsai.backend.entity.Project;
import java.time.LocalDateTime;

public class ProjectDto {

    private Long id;
    private String name;
    private String description;
    private String projectKey;
    private UserDto owner;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProjectDto() {
    }

    public ProjectDto(Long id, String name, String description, String projectKey, UserDto owner, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.projectKey = projectKey;
        this.owner = owner;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProjectDto fromEntity(Project project) {
        return new ProjectDto(
            project.getId(),
            project.getName(),
            project.getDescription(),
            project.getProjectKey(),
            project.getOwner() != null ? UserDto.fromEntity(project.getOwner()) : null,
            project.getCreatedAt(),
            project.getUpdatedAt()
        );
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

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public UserDto getOwner() {
        return owner;
    }

    public void setOwner(UserDto owner) {
        this.owner = owner;
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
