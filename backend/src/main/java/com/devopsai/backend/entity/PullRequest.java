package com.devopsai.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pull_requests")
public class PullRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int number;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PullRequestState state = PullRequestState.OPEN;

    @Column(name = "head_branch")
    private String headBranch;

    @Column(name = "base_branch")
    private String baseBranch;

    @Column(name = "author_username")
    private String authorUsername;

    @Column(name = "changed_files_count")
    private int changedFilesCount;

    private int additions;
    private int deletions;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "github_repository_id", nullable = false)
    private GithubRepository githubRepository;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public PullRequest() {
    }

    public PullRequest(int number, String title, String description, PullRequestState state,
                       String headBranch, String baseBranch, String authorUsername,
                       int changedFilesCount, int additions, int deletions,
                       GithubRepository githubRepository) {
        this.number = number;
        this.title = title;
        this.description = description;
        this.state = state != null ? state : PullRequestState.OPEN;
        this.headBranch = headBranch;
        this.baseBranch = baseBranch;
        this.authorUsername = authorUsername;
        this.changedFilesCount = changedFilesCount;
        this.additions = additions;
        this.deletions = deletions;
        this.githubRepository = githubRepository;
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

    public GithubRepository getGithubRepository() {
        return githubRepository;
    }

    public void setGithubRepository(GithubRepository githubRepository) {
        this.githubRepository = githubRepository;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
