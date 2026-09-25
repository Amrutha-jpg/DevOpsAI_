package com.devopsai.backend.dto;

public class SprintVelocityDto {

    private Long sprintId;
    private String sprintName;
    private int committedStoryPoints;
    private int completedStoryPoints;
    private String status;

    public SprintVelocityDto() {
    }

    public SprintVelocityDto(Long sprintId, String sprintName, int committedStoryPoints, int completedStoryPoints, String status) {
        this.sprintId = sprintId;
        this.sprintName = sprintName;
        this.committedStoryPoints = committedStoryPoints;
        this.completedStoryPoints = completedStoryPoints;
        this.status = status;
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

    public int getCommittedStoryPoints() {
        return committedStoryPoints;
    }

    public void setCommittedStoryPoints(int committedStoryPoints) {
        this.committedStoryPoints = committedStoryPoints;
    }

    public int getCompletedStoryPoints() {
        return completedStoryPoints;
    }

    public void setCompletedStoryPoints(int completedStoryPoints) {
        this.completedStoryPoints = completedStoryPoints;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
