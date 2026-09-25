package com.devopsai.backend.service;

import com.devopsai.backend.entity.Issue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PriorityCalculatorTest {

    private PriorityCalculator priorityCalculator;

    @BeforeEach
    void setUp() {
        priorityCalculator = new PriorityCalculator();
    }

    @Test
    @DisplayName("Should correctly calculate priority score for standard future due date")
    void testStandardCalculation() {
        // Formula: (Severity + BusinessImpact) / daysRemaining + dependencies
        // (8 + 6) / 4 + 2 = 14 / 4 + 2 = 3.5 + 2 = 5.5
        Issue issue = new Issue();
        issue.setSeverity(8);
        issue.setBusinessImpact(6);
        issue.setDueDate(LocalDate.now().plusDays(4));
        issue.setDependenciesCount(2);

        double score = priorityCalculator.calculateScore(issue);

        assertEquals(5.50, score, 0.01);
        assertEquals(5.50, issue.getCalculatedPriorityScore(), 0.01);
    }

    @Test
    @DisplayName("Should clamp overdue or due-today items to a minimum divisor of 1.0 to prevent division by zero")
    void testOverdueZeroDivisionProtection() {
        // Overdue item: daysRemaining <= 0 -> clamped to divisor 1.0
        // Formula: (10 + 9) / 1.0 + 3 = 19 + 3 = 22.0
        Issue overdueIssue = new Issue();
        overdueIssue.setSeverity(10);
        overdueIssue.setBusinessImpact(9);
        overdueIssue.setDueDate(LocalDate.now().minusDays(2)); // Overdue
        overdueIssue.setDependenciesCount(3);

        double score = priorityCalculator.calculateScore(overdueIssue);

        assertEquals(22.0, score, 0.01);
    }

    @Test
    @DisplayName("Should clamp due today items to a minimum divisor of 1.0")
    void testDueTodayZeroDivisionProtection() {
        // Due today: days = 0 -> clamped to 1.0
        // (7 + 5) / 1.0 + 0 = 12.0
        Issue dueTodayIssue = new Issue();
        dueTodayIssue.setSeverity(7);
        dueTodayIssue.setBusinessImpact(5);
        dueTodayIssue.setDueDate(LocalDate.now()); // Due today
        dueTodayIssue.setDependenciesCount(0);

        double score = priorityCalculator.calculateScore(dueTodayIssue);

        assertEquals(12.0, score, 0.01);
    }

    @Test
    @DisplayName("Should drain Max-Heap PriorityQueue in strict descending order of priority score")
    void testMaxHeapQueueDrainage() {
        Issue lowPriorityTask = new Issue();
        lowPriorityTask.setTitle("Low priority task");
        lowPriorityTask.setSeverity(2);
        lowPriorityTask.setBusinessImpact(2);
        lowPriorityTask.setDueDate(LocalDate.now().plusDays(10)); // divisor = 10 -> (2+2)/10 + 0 = 0.4
        lowPriorityTask.setDependenciesCount(0);

        Issue mediumTask = new Issue();
        mediumTask.setTitle("Medium priority task");
        mediumTask.setSeverity(5);
        mediumTask.setBusinessImpact(5);
        mediumTask.setDueDate(LocalDate.now().plusDays(2)); // divisor = 2 -> (5+5)/2 + 1 = 6.0
        mediumTask.setDependenciesCount(1);

        Issue criticalBug = new Issue();
        criticalBug.setTitle("Critical security bug");
        criticalBug.setSeverity(10);
        criticalBug.setBusinessImpact(10);
        criticalBug.setDueDate(LocalDate.now().minusDays(1)); // overdue -> divisor = 1.0 -> (10+10)/1 + 4 = 24.0
        criticalBug.setDependenciesCount(4);

        List<Issue> unranked = Arrays.asList(mediumTask, lowPriorityTask, criticalBug);
        List<Issue> ranked = priorityCalculator.rankIssues(unranked);

        assertEquals(3, ranked.size());
        assertEquals("Critical security bug", ranked.get(0).getTitle());
        assertEquals("Medium priority task", ranked.get(1).getTitle());
        assertEquals("Low priority task", ranked.get(2).getTitle());

        assertTrue(ranked.get(0).getCalculatedPriorityScore() > ranked.get(1).getCalculatedPriorityScore());
        assertTrue(ranked.get(1).getCalculatedPriorityScore() > ranked.get(2).getCalculatedPriorityScore());
    }
}
