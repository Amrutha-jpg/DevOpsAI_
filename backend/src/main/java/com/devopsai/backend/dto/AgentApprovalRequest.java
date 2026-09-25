package com.devopsai.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AgentApprovalRequest {

    @NotNull(message = "Task ID is required")
    private Long taskId;

    @NotBlank(message = "Decision is required (APPROVED or REJECTED)")
    private String decision; // APPROVED or REJECTED

    private String feedback;

    public AgentApprovalRequest() {
    }

    public AgentApprovalRequest(Long taskId, String decision, String feedback) {
        this.taskId = taskId;
        this.decision = decision;
        this.feedback = feedback;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
