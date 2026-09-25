package com.devopsai.backend.dto;

import java.util.List;

public class RagQueryResponse {

    private String answer;
    private double confidenceScore;
    private List<RagSourceDto> sources;
    private List<String> suggestedFollowups;

    public RagQueryResponse() {
    }

    public RagQueryResponse(String answer, double confidenceScore, List<RagSourceDto> sources, List<String> suggestedFollowups) {
        this.answer = answer;
        this.confidenceScore = confidenceScore;
        this.sources = sources;
        this.suggestedFollowups = suggestedFollowups;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public List<RagSourceDto> getSources() {
        return sources;
    }

    public void setSources(List<RagSourceDto> sources) {
        this.sources = sources;
    }

    public List<String> getSuggestedFollowups() {
        return suggestedFollowups;
    }

    public void setSuggestedFollowups(List<String> suggestedFollowups) {
        this.suggestedFollowups = suggestedFollowups;
    }
}
