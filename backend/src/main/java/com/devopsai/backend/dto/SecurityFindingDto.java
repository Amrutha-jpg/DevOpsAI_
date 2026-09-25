package com.devopsai.backend.dto;

import com.devopsai.backend.entity.SecurityScanCategory;
import com.devopsai.backend.entity.SecuritySeverity;

public class SecurityFindingDto {

    private String id;
    private String title;
    private String description;
    private SecuritySeverity severity;
    private SecurityScanCategory category;
    private String filePath;
    private int lineNumber;
    private String snippet;
    private String recommendation;
    private String cveId;

    public SecurityFindingDto() {
    }

    public SecurityFindingDto(String id, String title, String description, SecuritySeverity severity,
                              SecurityScanCategory category, String filePath, int lineNumber,
                              String snippet, String recommendation, String cveId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.category = category;
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.snippet = snippet;
        this.recommendation = recommendation;
        this.cveId = cveId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public SecuritySeverity getSeverity() {
        return severity;
    }

    public void setSeverity(SecuritySeverity severity) {
        this.severity = severity;
    }

    public SecurityScanCategory getCategory() {
        return category;
    }

    public void setCategory(SecurityScanCategory category) {
        this.category = category;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getCveId() {
        return cveId;
    }

    public void setCveId(String cveId) {
        this.cveId = cveId;
    }
}
