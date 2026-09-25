package com.devopsai.backend.dto;

public class CommitFrequencyDto {

    private String date; // e.g. "2026-09-17" or "Week 37"
    private int commitCount;
    private int additions;
    private int deletions;

    public CommitFrequencyDto() {
    }

    public CommitFrequencyDto(String date, int commitCount, int additions, int deletions) {
        this.date = date;
        this.commitCount = commitCount;
        this.additions = additions;
        this.deletions = deletions;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getCommitCount() {
        return commitCount;
    }

    public void setCommitCount(int commitCount) {
        this.commitCount = commitCount;
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
}
