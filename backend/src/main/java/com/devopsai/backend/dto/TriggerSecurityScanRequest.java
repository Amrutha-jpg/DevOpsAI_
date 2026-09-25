package com.devopsai.backend.dto;

public class TriggerSecurityScanRequest {

    private Long projectId;
    private String commitSha;
    private boolean simulateVulnerabilities;

    public TriggerSecurityScanRequest() {
    }

    public TriggerSecurityScanRequest(Long projectId, String commitSha, boolean simulateVulnerabilities) {
        this.projectId = projectId;
        this.commitSha = commitSha;
        this.simulateVulnerabilities = simulateVulnerabilities;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getCommitSha() {
        return commitSha;
    }

    public void setCommitSha(String commitSha) {
        this.commitSha = commitSha;
    }

    public boolean isSimulateVulnerabilities() {
        return simulateVulnerabilities;
    }

    public void setSimulateVulnerabilities(boolean simulateVulnerabilities) {
        this.simulateVulnerabilities = simulateVulnerabilities;
    }
}
