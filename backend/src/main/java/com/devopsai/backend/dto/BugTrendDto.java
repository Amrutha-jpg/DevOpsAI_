package com.devopsai.backend.dto;

public class BugTrendDto {

    private String period; // e.g. "2026-09-17"
    private int openBugs;
    private int resolvedBugs;
    private double mttrHours; // Mean Time to Resolve

    public BugTrendDto() {
    }

    public BugTrendDto(String period, int openBugs, int resolvedBugs, double mttrHours) {
        this.period = period;
        this.openBugs = openBugs;
        this.resolvedBugs = resolvedBugs;
        this.mttrHours = mttrHours;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public int getOpenBugs() {
        return openBugs;
    }

    public void setOpenBugs(int openBugs) {
        this.openBugs = openBugs;
    }

    public int getResolvedBugs() {
        return resolvedBugs;
    }

    public void setResolvedBugs(int resolvedBugs) {
        this.resolvedBugs = resolvedBugs;
    }

    public double getMttrHours() {
        return mttrHours;
    }

    public void setMttrHours(double mttrHours) {
        this.mttrHours = mttrHours;
    }
}
