package com.devopsai.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class AnalyzeSnippetRequest {

    private String filename = "Snippet.java";

    @NotBlank(message = "Code snippet content cannot be blank")
    private String codeSnippet;

    private Long projectId;

    public AnalyzeSnippetRequest() {
    }

    public AnalyzeSnippetRequest(String filename, String codeSnippet, Long projectId) {
        this.filename = filename;
        this.codeSnippet = codeSnippet;
        this.projectId = projectId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getCodeSnippet() {
        return codeSnippet;
    }

    public void setCodeSnippet(String codeSnippet) {
        this.codeSnippet = codeSnippet;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
