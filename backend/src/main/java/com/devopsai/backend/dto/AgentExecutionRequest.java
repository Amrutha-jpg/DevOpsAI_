package com.devopsai.backend.dto;

import com.devopsai.backend.entity.AgentType;
import jakarta.validation.constraints.NotNull;

public class AgentExecutionRequest {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotNull(message = "Agent Type is required")
    private AgentType agentType;

    private Long targetId;
    private String inputPayload;

    public AgentExecutionRequest() {
    }

    public AgentExecutionRequest(Long projectId, AgentType agentType, Long targetId, String inputPayload) {
        this.projectId = projectId;
        this.agentType = agentType;
        this.targetId = targetId;
        this.inputPayload = inputPayload;
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

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getInputPayload() {
        return inputPayload;
    }

    public void setInputPayload(String inputPayload) {
        this.inputPayload = inputPayload;
    }
}
