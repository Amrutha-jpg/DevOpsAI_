package com.devopsai.backend.dto;

import com.devopsai.backend.entity.AgentTaskStatus;
import com.devopsai.backend.entity.AgentType;

import java.time.LocalDateTime;
import java.util.List;

public class AgentTaskResponseDto {

    private Long taskId;
    private Long projectId;
    private AgentType agentType;
    private AgentTaskStatus status;
    private String inputPayload;
    private List<String> executionSteps;
    private String outputResult;
    private String approvedBy;
    private String humanFeedback;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AgentTaskResponseDto() {
    }

    public AgentTaskResponseDto(Long taskId, Long projectId, AgentType agentType, AgentTaskStatus status,
                                String inputPayload, List<String> executionSteps, String outputResult,
                                String approvedBy, String humanFeedback, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.taskId = taskId;
        this.projectId = projectId;
        this.agentType = agentType;
        this.status = status;
        this.inputPayload = inputPayload;
        this.executionSteps = executionSteps;
        this.outputResult = outputResult;
        this.approvedBy = approvedBy;
        this.humanFeedback = humanFeedback;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public AgentType getAgentType() {
        return agentType;
    }

    public void setAgentType(AgentType agentType) {
        this.agentType = agentType;
    }

    public AgentTaskStatus getStatus() {
        return status;
    }

    public void setStatus(AgentTaskStatus status) {
        this.status = status;
    }

    public String getInputPayload() {
        return inputPayload;
    }

    public void setInputPayload(String inputPayload) {
        this.inputPayload = inputPayload;
    }

    public List<String> getExecutionSteps() {
        return executionSteps;
    }

    public void setExecutionSteps(List<String> executionSteps) {
        this.executionSteps = executionSteps;
    }

    public String getOutputResult() {
        return outputResult;
    }

    public void setOutputResult(String outputResult) {
        this.outputResult = outputResult;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getHumanFeedback() {
        return humanFeedback;
    }

    public void setHumanFeedback(String humanFeedback) {
        this.humanFeedback = humanFeedback;
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
