package com.devopsai.backend.service;

import com.devopsai.backend.entity.Issue;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

@Service
public class PriorityCalculator {

    /**
     * Calculates the Priority Score for an issue based on the formula:
     * Priority Score = (Severity + Business Impact) / max(Days Remaining, 1.0) + Dependencies Count
     */
    public double calculateScore(Issue issue) {
        if (issue == null) {
            return 0.0;
        }

        double severity = (double) Math.max(1, Math.min(10, issue.getSeverity()));
        double businessImpact = (double) Math.max(1, Math.min(10, issue.getBusinessImpact()));

        double daysRemaining = 1.0;
        if (issue.getDueDate() != null) {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), issue.getDueDate());
            // Overdue or due-today items (days <= 0) clamped to a minimum divisor of 1.0
            daysRemaining = Math.max(1.0, (double) days);
        }

        double dependency = (double) Math.max(0, issue.getDependenciesCount());

        double rawScore = ((severity + businessImpact) / daysRemaining) + dependency;

        BigDecimal bd = BigDecimal.valueOf(rawScore).setScale(2, RoundingMode.HALF_UP);
        double score = bd.doubleValue();
        issue.setCalculatedPriorityScore(score);
        return score;
    }

    /**
     * Ranks a list of issues using a Max-Heap PriorityQueue.
     * Drains the heap to return issues in strict descending order of priority score.
     */
    public List<Issue> rankIssues(List<Issue> issues) {
        if (issues == null || issues.isEmpty()) {
            return new ArrayList<>();
        }

        // Configure PriorityQueue as a Max-Heap based on calculatedPriorityScore
        PriorityQueue<Issue> maxHeap = new PriorityQueue<>(
            (a, b) -> Double.compare(b.getCalculatedPriorityScore(), a.getCalculatedPriorityScore())
        );

        for (Issue issue : issues) {
            calculateScore(issue);
            maxHeap.offer(issue);
        }

        List<Issue> rankedIssues = new ArrayList<>();
        while (!maxHeap.isEmpty()) {
            rankedIssues.add(maxHeap.poll());
        }

        return rankedIssues;
    }
}
