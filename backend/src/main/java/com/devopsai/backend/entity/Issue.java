package com.devopsai.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "issues")
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueType type = IssueType.TASK;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus status = IssueStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssuePriority priority = IssuePriority.MEDIUM;

    @Column(nullable = false)
    private int severity = 5; // 1 - 10

    @Column(name = "business_impact", nullable = false)
    private int businessImpact = 5; // 1 - 10

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "dependencies_count", nullable = false)
    private int dependenciesCount = 0;

    @Column(name = "calculated_priority_score")
    private double calculatedPriorityScore = 0.0;

    @Column(name = "story_points", nullable = false)
    private int storyPoints = 1;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "epic_id")
    private Epic epic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Issue() {
    }

    public Issue(String title, String description, IssueType type, IssueStatus status,
                 IssuePriority priority, int severity, int businessImpact,
                 LocalDate dueDate, int dependenciesCount, Project project, User reporter) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.status = status != null ? status : IssueStatus.TODO;
        this.priority = priority != null ? priority : IssuePriority.MEDIUM;
        this.severity = severity;
        this.businessImpact = businessImpact;
        this.dueDate = dueDate;
        this.dependenciesCount = dependenciesCount;
        this.project = project;
        this.reporter = reporter;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public IssueType getType() {
        return type;
    }

    public void setType(IssueType type) {
        this.type = type;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }

    public IssuePriority getPriority() {
        return priority;
    }

    public void setPriority(IssuePriority priority) {
        this.priority = priority;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public int getBusinessImpact() {
        return businessImpact;
    }

    public void setBusinessImpact(int businessImpact) {
        this.businessImpact = businessImpact;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public int getDependenciesCount() {
        return dependenciesCount;
    }

    public void setDependenciesCount(int dependenciesCount) {
        this.dependenciesCount = dependenciesCount;
    }

    public double getCalculatedPriorityScore() {
        return calculatedPriorityScore;
    }

    public void setCalculatedPriorityScore(double calculatedPriorityScore) {
        this.calculatedPriorityScore = calculatedPriorityScore;
    }

    public int getStoryPoints() {
        return storyPoints;
    }

    public void setStoryPoints(int storyPoints) {
        this.storyPoints = storyPoints > 0 ? storyPoints : 1;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Epic getEpic() {
        return epic;
    }

    public void setEpic(Epic epic) {
        this.epic = epic;
    }

    public Sprint getSprint() {
        return sprint;
    }

    public void setSprint(Sprint sprint) {
        this.sprint = sprint;
    }

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public User getReporter() {
        return reporter;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
