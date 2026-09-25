package com.devopsai.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "changed_files")
public class ChangedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String status; // added, modified, removed

    private int additions;
    private int deletions;

    @Column(columnDefinition = "TEXT")
    private String patch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pull_request_id", nullable = false)
    private PullRequest pullRequest;

    public ChangedFile() {
    }

    public ChangedFile(String filename, String status, int additions, int deletions, String patch, PullRequest pullRequest) {
        this.filename = filename;
        this.status = status;
        this.additions = additions;
        this.deletions = deletions;
        this.patch = patch;
        this.pullRequest = pullRequest;
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

    public PullRequest getPullRequest() {
        return pullRequest;
    }

    public void setPullRequest(PullRequest pullRequest) {
        this.pullRequest = pullRequest;
    }
}
