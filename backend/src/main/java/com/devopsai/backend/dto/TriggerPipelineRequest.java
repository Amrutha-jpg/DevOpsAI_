package com.devopsai.backend.dto;

public class TriggerPipelineRequest {

    private String targetEnvironment; // STAGING, PRODUCTION
    private String commitSha;
    private String branch;

    public TriggerPipelineRequest() {}

    public TriggerPipelineRequest(String branchOrEnv, String commitSha) {
        if (branchOrEnv != null && (branchOrEnv.equalsIgnoreCase("PRODUCTION") || branchOrEnv.equalsIgnoreCase("STAGING"))) {
            this.targetEnvironment = branchOrEnv;
            this.branch = "main";
        } else {
            this.branch = branchOrEnv;
            this.targetEnvironment = "PRODUCTION";
        }
        this.commitSha = commitSha;
    }

    public TriggerPipelineRequest(String targetEnvironment, String commitSha, String branch) {
        this.targetEnvironment = targetEnvironment;
        this.commitSha = commitSha;
        this.branch = branch;
    }

    public String getTargetEnvironment() {
        return targetEnvironment != null ? targetEnvironment : "PRODUCTION";
    }

    public void setTargetEnvironment(String targetEnvironment) {
        this.targetEnvironment = targetEnvironment;
    }

    public String getCommitSha() {
        return commitSha;
    }

    public void setCommitSha(String commitSha) {
        this.commitSha = commitSha;
    }

    public String getBranch() {
        return branch != null ? branch : "main";
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }
}
