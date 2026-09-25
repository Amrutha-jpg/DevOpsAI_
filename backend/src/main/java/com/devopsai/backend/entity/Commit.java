package com.devopsai.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "commits")
public class Commit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String sha;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "author_email")
    private String authorEmail;

    @Column(name = "commit_date")
    private LocalDateTime commitDate;

    @Column(name = "html_url")
    private String htmlUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "github_repository_id", nullable = false)
    private GithubRepository githubRepository;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Commit() {
    }

    public Commit(String sha, String message, String authorName, String authorEmail, LocalDateTime commitDate, String htmlUrl, GithubRepository githubRepository) {
        this.sha = sha;
        this.message = message;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.commitDate = commitDate;
        this.htmlUrl = htmlUrl;
        this.githubRepository = githubRepository;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
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

    public GithubRepository getGithubRepository() {
        return githubRepository;
    }

    public void setGithubRepository(GithubRepository githubRepository) {
        this.githubRepository = githubRepository;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
