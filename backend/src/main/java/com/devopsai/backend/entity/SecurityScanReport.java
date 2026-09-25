package com.devopsai.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "security_scan_reports")
public class SecurityScanReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "scanned_commit_sha", nullable = false)
    private String scannedCommitSha;

    @Column(nullable = false)
    private String status = "PASSED"; // PASSED, WARNINGS, FAILED

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_risk_level", nullable = false)
    private SecurityRiskLevel overallRiskLevel = SecurityRiskLevel.LOW;

    @Column(name = "critical_count")
    private int criticalCount;

    @Column(name = "high_count")
    private int highCount;

    @Column(name = "medium_count")
    private int mediumCount;

    @Column(name = "low_count")
    private int lowCount;

    @Column(name = "overall_security_score")
    private double overallSecurityScore = 100.0;

    @Column(name = "findings_json", columnDefinition = "TEXT")
    private String findingsJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public SecurityScanReport() {
    }

    public SecurityScanReport(Long projectId, String scannedCommitSha) {
        this.projectId = projectId;
        this.scannedCommitSha = scannedCommitSha != null ? scannedCommitSha : "head";
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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getScannedCommitSha() {
        return scannedCommitSha;
    }

    public void setScannedCommitSha(String scannedCommitSha) {
        this.scannedCommitSha = scannedCommitSha;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public SecurityRiskLevel getOverallRiskLevel() {
        return overallRiskLevel;
    }

    public void setOverallRiskLevel(SecurityRiskLevel overallRiskLevel) {
        this.overallRiskLevel = overallRiskLevel;
    }

    public int getCriticalCount() {
        return criticalCount;
    }

    public void setCriticalCount(int criticalCount) {
        this.criticalCount = criticalCount;
    }

    public int getHighCount() {
        return highCount;
    }

    public void setHighCount(int highCount) {
        this.highCount = highCount;
    }

    public int getMediumCount() {
        return mediumCount;
    }

    public void setMediumCount(int mediumCount) {
        this.mediumCount = mediumCount;
    }

    public int getLowCount() {
        return lowCount;
    }

    public void setLowCount(int lowCount) {
        this.lowCount = lowCount;
    }

    public double getOverallSecurityScore() {
        return overallSecurityScore;
    }

    public void setOverallSecurityScore(double overallSecurityScore) {
        this.overallSecurityScore = overallSecurityScore;
    }

    public String getFindingsJson() {
        return findingsJson;
    }

    public void setFindingsJson(String findingsJson) {
        this.findingsJson = findingsJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
