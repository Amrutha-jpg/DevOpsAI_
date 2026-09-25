package com.devopsai.backend.dto;

import com.devopsai.backend.entity.Sprint;
import com.devopsai.backend.entity.SprintStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SprintDto {

    private Long id;
    private String name;
    private String goal;
    private LocalDate startDate;
    private LocalDate endDate;
    private SprintStatus status;
    private Long projectId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SprintDto() {
    }

    public SprintDto(Sprint sprint) {
        this.id = sprint.getId();
        this.name = sprint.getName();
        this.goal = sprint.getGoal();
        this.startDate = sprint.getStartDate();
        this.endDate = sprint.getEndDate();
        this.status = sprint.getStatus();
        this.projectId = sprint.getProject() != null ? sprint.getProject().getId() : null;
        this.createdAt = sprint.getCreatedAt();
        this.updatedAt = sprint.getUpdatedAt();
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

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public SprintStatus getStatus() {
        return status;
    }

    public void setStatus(SprintStatus status) {
        this.status = status;
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
