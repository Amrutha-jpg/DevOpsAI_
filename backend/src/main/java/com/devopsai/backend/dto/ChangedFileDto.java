package com.devopsai.backend.dto;

import com.devopsai.backend.entity.ChangedFile;

public class ChangedFileDto {

    private Long id;
    private String filename;
    private String status;
    private int additions;
    private int deletions;
    private String patch;
    private Long pullRequestId;

    public ChangedFileDto() {
    }

    public ChangedFileDto(ChangedFile cf) {
        this.id = cf.getId();
        this.filename = cf.getFilename();
        this.status = cf.getStatus();
        this.additions = cf.getAdditions();
        this.deletions = cf.getDeletions();
        this.patch = cf.getPatch();
        this.pullRequestId = cf.getPullRequest() != null ? cf.getPullRequest().getId() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAdditions() {
        return additions;
    }

    public void setAdditions(int additions) {
        this.additions = additions;
    }

    public int getDeletions() {
        return deletions;
    }

    public void setDeletions(int deletions) {
        this.deletions = deletions;
    }

    public String getPatch() {
        return patch;
    }

    public void setPatch(String patch) {
        this.patch = patch;
    }

    public Long getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(Long pullRequestId) {
        this.pullRequestId = pullRequestId;
    }
}
