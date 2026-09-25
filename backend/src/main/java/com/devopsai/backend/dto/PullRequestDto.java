package com.devopsai.backend.dto;

import com.devopsai.backend.entity.PullRequest;
import com.devopsai.backend.entity.PullRequestState;

import java.time.LocalDateTime;

public class PullRequestDto {

    private Long id;
    private int number;
    private String title;
    private String description;
    private PullRequestState state;
    private String headBranch;
    private String baseBranch;
    private String authorUsername;
    private int changedFilesCount;
    private int additions;
    private int deletions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PullRequestDto() {
    }

    public PullRequestDto(PullRequest pr) {
        this.id = pr.getId();
        this.number = pr.getNumber();
        this.title = pr.getTitle();
        this.description = pr.getDescription();
        this.state = pr.getState();
        this.headBranch = pr.getHeadBranch();
        this.baseBranch = pr.getBaseBranch();
        this.authorUsername = pr.getAuthorUsername();
        this.changedFilesCount = pr.getChangedFilesCount();
        this.additions = pr.getAdditions();
        this.deletions = pr.getDeletions();
        this.createdAt = pr.getCreatedAt();
        this.updatedAt = pr.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
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

    public PullRequestState getState() {
        return state;
    }

    public void setState(PullRequestState state) {
        this.state = state;
    }

    public String getHeadBranch() {
        return headBranch;
    }

    public void setHeadBranch(String headBranch) {
        this.headBranch = headBranch;
    }

    public String getBaseBranch() {
        return baseBranch;
    }

    public void setBaseBranch(String baseBranch) {
        this.baseBranch = baseBranch;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public int getChangedFilesCount() {
        return changedFilesCount;
    }

    public void setChangedFilesCount(int changedFilesCount) {
        this.changedFilesCount = changedFilesCount;
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
