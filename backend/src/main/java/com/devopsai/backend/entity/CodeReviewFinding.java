package com.devopsai.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "code_review_findings")
public class CodeReviewFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FindingSeverity severity;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    @Column(name = "code_snippet", columnDefinition = "TEXT")
    private String codeSnippet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "code_review_id", nullable = false)
    private CodeReview codeReview;

    public CodeReviewFinding() {
    }

    public CodeReviewFinding(String filename, Integer lineNumber, ReviewCategory category,
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

    public CodeReviewFinding(String filename, Integer lineNumber, ReviewCategory category,
                             FindingSeverity severity, String title, String description,
                             String recommendation, String codeSnippet, CodeReview codeReview) {
        this(filename, lineNumber, category, severity, title, description, recommendation, codeSnippet);
        this.codeReview = codeReview;
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

    public CodeReview getCodeReview() {
        return codeReview;
    }

    public void setCodeReview(CodeReview codeReview) {
        this.codeReview = codeReview;
    }
}
