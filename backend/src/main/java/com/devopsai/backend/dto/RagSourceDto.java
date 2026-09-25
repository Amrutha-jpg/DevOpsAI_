package com.devopsai.backend.dto;

public class RagSourceDto {

    private String filename;
    private int startLine;
    private int endLine;
    private double relevanceScore;
    private String snippet;
    private String chunkType;

    public RagSourceDto() {
    }

    public RagSourceDto(String filename, int startLine, int endLine, double relevanceScore, String snippet, String chunkType) {
        this.filename = filename;
        this.startLine = startLine;
        this.endLine = endLine;
        this.relevanceScore = relevanceScore;
        this.snippet = snippet;
        this.chunkType = chunkType;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public double getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(double relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getChunkType() {
        return chunkType;
    }

    public void setChunkType(String chunkType) {
        this.chunkType = chunkType;
    }
}
