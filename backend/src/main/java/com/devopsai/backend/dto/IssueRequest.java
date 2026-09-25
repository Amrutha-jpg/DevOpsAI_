package com.devopsai.backend.dto;

import com.devopsai.backend.entity.IssuePriority;
import com.devopsai.backend.entity.IssueStatus;
import com.devopsai.backend.entity.IssueType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class IssueRequest {

    @NotBlank(message = "Issue title is required")
    @Size(min = 3, max = 200, message = "Issue title must be between 3 and 200 characters")
    private String title;

    private String description;
    private IssueType type = IssueType.TASK;
    private IssueStatus status = IssueStatus.TODO;
    private IssuePriority priority = IssuePriority.MEDIUM;

    @Min(value = 1, message = "Severity must be between 1 and 10")
    @Max(value = 10, message = "Severity must be between 1 and 10")
    private int severity = 5;

    @Min(value = 1, message = "Business impact must be between 1 and 10")
    @Max(value = 10, message = "Business impact must be between 1 and 10")
    private int businessImpact = 5;

    private LocalDate dueDate;

    @Min(value = 0, message = "Dependencies count cannot be negative")
    private int dependenciesCount = 0;

    private Long epicId;
    private Long sprintId;
    private Long assigneeId;

    public IssueRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IssueType getType() {
        return type;
    }

    public void setType(IssueType type) {
        this.type = type;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }

    public IssuePriority getPriority() {
        return priority;
    }

    public void setPriority(IssuePriority priority) {
        this.priority = priority;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public int getBusinessImpact() {
        return businessImpact;
    }

    public void setBusinessImpact(int businessImpact) {
        this.businessImpact = businessImpact;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public int getDependenciesCount() {
        return dependenciesCount;
    }

    public void setDependenciesCount(int dependenciesCount) {
        this.dependenciesCount = dependenciesCount;
    }

    public Long getEpicId() {
        return epicId;
    }

    public void setEpicId(Long epicId) {
        this.epicId = epicId;
    }

    public Long getSprintId() {
        return sprintId;
    }

    public void setSprintId(Long sprintId) {
        this.sprintId = sprintId;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }
}
