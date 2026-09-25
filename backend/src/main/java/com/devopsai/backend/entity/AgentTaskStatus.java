package com.devopsai.backend.entity;

public enum AgentTaskStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    REQUIRES_HUMAN_APPROVAL,
    APPROVED,
    REJECTED,
    FAILED
}
