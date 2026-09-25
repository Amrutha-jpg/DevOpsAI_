package com.devopsai.backend.service;

import com.devopsai.backend.dto.IssueDto;
import com.devopsai.backend.dto.IssueRequest;
import com.devopsai.backend.entity.*;
import com.devopsai.backend.exception.ResourceNotFoundException;
import com.devopsai.backend.repository.*;
import com.devopsai.backend.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class IssueService {

    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final EpicRepository epicRepository;
    private final SprintRepository sprintRepository;
    private final PriorityCalculator priorityCalculator;

    public IssueService(IssueRepository issueRepository,
                        ProjectRepository projectRepository,
                        UserRepository userRepository,
                        EpicRepository epicRepository,
                        SprintRepository sprintRepository,
                        PriorityCalculator priorityCalculator) {
        this.issueRepository = issueRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.epicRepository = epicRepository;
        this.sprintRepository = sprintRepository;
        this.priorityCalculator = priorityCalculator;
    }

    @Transactional
    public IssueDto createIssue(Long projectId, IssueRequest request, Authentication authentication) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User reporter = userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + principal.getId()));

        Issue issue = new Issue(
            request.getTitle(),
            request.getDescription(),
            request.getType(),
            request.getStatus(),
            request.getPriority(),
            request.getSeverity(),
            request.getBusinessImpact(),
            request.getDueDate(),
            request.getDependenciesCount(),
            project,
            reporter
        );

        if (request.getEpicId() != null) {
            Epic epic = epicRepository.findById(request.getEpicId())
                .orElseThrow(() -> new ResourceNotFoundException("Epic not found with id: " + request.getEpicId()));
            issue.setEpic(epic);
        }

        if (request.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(request.getSprintId())
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id: " + request.getSprintId()));
            issue.setSprint(sprint);
        }

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + request.getAssigneeId()));
            issue.setAssignee(assignee);
        }

        priorityCalculator.calculateScore(issue);
        Issue savedIssue = issueRepository.save(issue);
        return new IssueDto(savedIssue);
    }

    @Transactional(readOnly = true)
    public List<IssueDto> getIssuesForProject(Long projectId, Long sprintId, Long epicId, IssueStatus status) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }

        List<Issue> issues;
        if (sprintId != null) {
            issues = issueRepository.findByProjectIdAndSprintId(projectId, sprintId);
        } else if (epicId != null) {
            issues = issueRepository.findByProjectIdAndEpicId(projectId, epicId);
        } else if (status != null) {
            issues = issueRepository.findByProjectIdAndStatus(projectId, status);
        } else {
            issues = issueRepository.findByProjectId(projectId);
        }

        return issues.stream()
            .map(IssueDto::new)
            .collect(Collectors.toList());
    }

    @Transactional
    public List<IssueDto> getPrioritizedBacklog(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }

        List<Issue> issues = issueRepository.findByProjectId(projectId);
        List<Issue> rankedIssues = priorityCalculator.rankIssues(issues);
        
        // Persist updated scores
        issueRepository.saveAll(rankedIssues);

        return rankedIssues.stream()
            .map(IssueDto::new)
            .collect(Collectors.toList());
    }

    @Transactional
    public IssueDto updateIssueStatus(Long issueId, IssueStatus newStatus) {
        Issue issue = issueRepository.findById(issueId)
            .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + issueId));

        issue.setStatus(newStatus);
        Issue updatedIssue = issueRepository.save(issue);
        return new IssueDto(updatedIssue);
    }

    @Transactional
    public IssueDto updateIssue(Long issueId, IssueRequest request) {
        Issue issue = issueRepository.findById(issueId)
            .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + issueId));

        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setType(request.getType());
        if (request.getStatus() != null) {
            issue.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            issue.setPriority(request.getPriority());
        }
        issue.setSeverity(request.getSeverity());
        issue.setBusinessImpact(request.getBusinessImpact());
        issue.setDueDate(request.getDueDate());
        issue.setDependenciesCount(request.getDependenciesCount());

        if (request.getEpicId() != null) {
            Epic epic = epicRepository.findById(request.getEpicId())
                .orElseThrow(() -> new ResourceNotFoundException("Epic not found with id: " + request.getEpicId()));
            issue.setEpic(epic);
        } else {
            issue.setEpic(null);
        }

        if (request.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(request.getSprintId())
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id: " + request.getSprintId()));
            issue.setSprint(sprint);
        } else {
            issue.setSprint(null);
        }

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + request.getAssigneeId()));
            issue.setAssignee(assignee);
        } else {
            issue.setAssignee(null);
        }

        priorityCalculator.calculateScore(issue);
        Issue savedIssue = issueRepository.save(issue);
        return new IssueDto(savedIssue);
    }

    @Transactional
    public void deleteIssue(Long issueId) {
        Issue issue = issueRepository.findById(issueId)
            .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + issueId));
        issueRepository.delete(issue);
    }
}
