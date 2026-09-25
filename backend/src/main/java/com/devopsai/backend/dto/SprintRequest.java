package com.devopsai.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class SprintRequest {

    @NotBlank(message = "Sprint name is required")
    @Size(min = 2, max = 100, message = "Sprint name must be between 2 and 100 characters")
    private String name;

    private String goal;
    private LocalDate startDate;
    private LocalDate endDate;

    public SprintRequest() {
    }

    public SprintRequest(String name, String goal, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.goal = goal;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
