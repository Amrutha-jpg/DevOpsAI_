package com.devopsai.backend.dto;

import com.devopsai.backend.entity.Commit;

import java.time.LocalDateTime;

public class CommitDto {

    private Long id;
    private String sha;
    private String message;
    private String authorName;
    private String authorEmail;
    private LocalDateTime commitDate;
    private String htmlUrl;

    public CommitDto() {
    }

    public CommitDto(Commit commit) {
        this.id = commit.getId();
        this.sha = commit.getSha();
        this.message = commit.getMessage();
        this.authorName = commit.getAuthorName();
        this.authorEmail = commit.getAuthorEmail();
        this.commitDate = commit.getCommitDate();
        this.htmlUrl = commit.getHtmlUrl();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public LocalDateTime getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(LocalDateTime commitDate) {
        this.commitDate = commitDate;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }
}
