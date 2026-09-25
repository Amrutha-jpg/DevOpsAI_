package com.devopsai.backend.dto;

import com.devopsai.backend.entity.SecurityRiskLevel;

import java.time.LocalDateTime;
import java.util.List;

public class SecurityScanReportDto {

    private Long id;
    private Long projectId;
    private String scannedCommitSha;
    private String status;
    private SecurityRiskLevel overallRiskLevel;
    private int criticalCount;
    private int highCount;
    private int mediumCount;
    private int lowCount;
    private double overallSecurityScore;
    private List<SecurityFindingDto> findings;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SecurityScanReportDto() {
    }

    public SecurityScanReportDto(Long id, Long projectId, String scannedCommitSha, String status,
                                SecurityRiskLevel overallRiskLevel, int criticalCount, int highCount,
                                int mediumCount, int lowCount, double overallSecurityScore,
                                List<SecurityFindingDto> findings, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.projectId = projectId;
        this.scannedCommitSha = scannedCommitSha;
        this.status = status;
        this.overallRiskLevel = overallRiskLevel;
        this.criticalCount = criticalCount;
        this.highCount = highCount;
        this.mediumCount = mediumCount;
        this.lowCount = lowCount;
        this.overallSecurityScore = overallSecurityScore;
        this.findings = findings;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public List<SecurityFindingDto> getFindings() {
        return findings;
    }

    public void setFindings(List<SecurityFindingDto> findings) {
        this.findings = findings;
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
