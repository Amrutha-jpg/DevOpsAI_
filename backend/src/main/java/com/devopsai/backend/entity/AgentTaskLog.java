package com.devopsai.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agent_task_logs")
public class AgentTaskLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "agent_type", nullable = false)
    private AgentType agentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgentTaskStatus status = AgentTaskStatus.PENDING;

    @Column(name = "input_payload", columnDefinition = "TEXT")
    private String inputPayload;

    @Column(name = "execution_steps_json", columnDefinition = "TEXT")
    private String executionStepsJson; // JSON array of step descriptions and tool outputs

    @Column(name = "output_result_json", columnDefinition = "TEXT")
    private String outputResultJson; // Final generated code/patch/docs

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "human_feedback", columnDefinition = "TEXT")
    private String humanFeedback;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public AgentTaskLog() {
    }

    public AgentTaskLog(Long projectId, AgentType agentType, String inputPayload) {
        this.projectId = projectId;
        this.agentType = agentType;
        this.inputPayload = inputPayload;
        this.status = AgentTaskStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getExecutionStepsJson() {
        return executionStepsJson;
    }

    public void setExecutionStepsJson(String executionStepsJson) {
        this.executionStepsJson = executionStepsJson;
    }

    public String getOutputResultJson() {
        return outputResultJson;
    }

    public void setOutputResultJson(String outputResultJson) {
        this.outputResultJson = outputResultJson;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
