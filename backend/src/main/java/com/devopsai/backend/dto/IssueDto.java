package com.devopsai.backend.dto;

import com.devopsai.backend.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class IssueDto {

    private Long id;
    private String title;
    private String description;
    private IssueType type;
    private IssueStatus status;
    private IssuePriority priority;
    private int severity;
    private int businessImpact;
    private LocalDate dueDate;
    private int dependenciesCount;
    private double calculatedPriorityScore;
    private int storyPoints;
    private Long projectId;
    private Long epicId;
    private String epicName;
    private Long sprintId;
    private String sprintName;
    private Long assigneeId;
    private String assigneeUsername;
    private Long reporterId;
    private String reporterUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public IssueDto() {
    }

    public IssueDto(Issue issue) {
        this.id = issue.getId();
        this.title = issue.getTitle();
        this.description = issue.getDescription();
        this.type = issue.getType();
        this.status = issue.getStatus();
        this.priority = issue.getPriority();
        this.severity = issue.getSeverity();
        this.businessImpact = issue.getBusinessImpact();
        this.dueDate = issue.getDueDate();
        this.dependenciesCount = issue.getDependenciesCount();
        this.calculatedPriorityScore = issue.getCalculatedPriorityScore();
        this.storyPoints = issue.getStoryPoints();
        this.projectId = issue.getProject() != null ? issue.getProject().getId() : null;

        if (issue.getEpic() != null) {
            this.epicId = issue.getEpic().getId();
            this.epicName = issue.getEpic().getName();
        }

        if (issue.getSprint() != null) {
            this.sprintId = issue.getSprint().getId();
            this.sprintName = issue.getSprint().getName();
        }

        if (issue.getAssignee() != null) {
            this.assigneeId = issue.getAssignee().getId();
            this.assigneeUsername = issue.getAssignee().getUsername();
        }

        if (issue.getReporter() != null) {
            this.reporterId = issue.getReporter().getId();
            this.reporterUsername = issue.getReporter().getUsername();
        }

        this.createdAt = issue.getCreatedAt();
        this.updatedAt = issue.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public double getCalculatedPriorityScore() {
        return calculatedPriorityScore;
    }

    public void setCalculatedPriorityScore(double calculatedPriorityScore) {
        this.calculatedPriorityScore = calculatedPriorityScore;
    }

    public int getStoryPoints() {
        return storyPoints;
    }

    public void setStoryPoints(int storyPoints) {
        this.storyPoints = storyPoints;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getEpicId() {
        return epicId;
    }

    public void setEpicId(Long epicId) {
        this.epicId = epicId;
    }

    public String getEpicName() {
        return epicName;
    }

    public void setEpicName(String epicName) {
        this.epicName = epicName;
    }

    public Long getSprintId() {
        return sprintId;
    }

    public void setSprintId(Long sprintId) {
        this.sprintId = sprintId;
    }

    public String getSprintName() {
        return sprintName;
    }

    public void setSprintName(String sprintName) {
        this.sprintName = sprintName;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public String getAssigneeUsername() {
        return assigneeUsername;
    }

    public void setAssigneeUsername(String assigneeUsername) {
        this.assigneeUsername = assigneeUsername;
    }

    public Long getReporterId() {
        return reporterId;
    }

    public void setReporterId(Long reporterId) {
        this.reporterId = reporterId;
    }

    public String getReporterUsername() {
        return reporterUsername;
    }

    public void setReporterUsername(String reporterUsername) {
        this.reporterUsername = reporterUsername;
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
