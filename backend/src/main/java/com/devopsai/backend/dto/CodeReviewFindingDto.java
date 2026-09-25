package com.devopsai.backend.dto;

import com.devopsai.backend.entity.CodeReviewFinding;
import com.devopsai.backend.entity.FindingSeverity;
import com.devopsai.backend.entity.ReviewCategory;

public class CodeReviewFindingDto {

    private Long id;
    private String filename;
    private Integer lineNumber;
    private ReviewCategory category;
    private FindingSeverity severity;
    private String title;
    private String description;
    private String recommendation;
    private String codeSnippet;

    public CodeReviewFindingDto() {
    }

    public CodeReviewFindingDto(CodeReviewFinding finding) {
        this.id = finding.getId();
        this.filename = finding.getFilename();
        this.lineNumber = finding.getLineNumber();
        this.category = finding.getCategory();
        this.severity = finding.getSeverity();
        this.title = finding.getTitle();
        this.description = finding.getDescription();
        this.recommendation = finding.getRecommendation();
        this.codeSnippet = finding.getCodeSnippet();
    }

    public CodeReviewFindingDto(String filename, Integer lineNumber, ReviewCategory category,
                               FindingSeverity severity, String title, String description,
                               String recommendation, String codeSnippet) {
        this.filename = filename;
        this.lineNumber = lineNumber;
        this.category = category;
        this.severity = severity;
        this.title = title;
        this.description = description;
        this.recommendation = recommendation;
        this.codeSnippet = codeSnippet;
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

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public ReviewCategory getCategory() {
        return category;
    }

    public void setCategory(ReviewCategory category) {
        this.category = category;
    }

    public FindingSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(FindingSeverity severity) {
        this.severity = severity;
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

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getCodeSnippet() {
        return codeSnippet;
    }

    public void setCodeSnippet(String codeSnippet) {
        this.codeSnippet = codeSnippet;
    }
}
