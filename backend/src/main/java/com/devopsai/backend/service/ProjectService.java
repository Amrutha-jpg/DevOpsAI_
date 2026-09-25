package com.devopsai.backend.service;

import com.devopsai.backend.dto.ProjectDto;
import com.devopsai.backend.dto.ProjectRequest;
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
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final CodebaseChunkRepository codebaseChunkRepository;
    private final AgentTaskLogRepository agentTaskLogRepository;
    private final SecurityScanReportRepository securityScanReportRepository;
    private final CodeReviewRepository codeReviewRepository;
    private final TestPipelineRunRepository testPipelineRunRepository;
    private final GithubRepositoryRepository githubRepositoryRepository;
    private final PullRequestRepository pullRequestRepository;
    private final CommitRepository commitRepository;
    private final ChangedFileRepository changedFileRepository;
    private final IssueRepository issueRepository;
    private final EpicRepository epicRepository;
    private final SprintRepository sprintRepository;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository projectMemberRepository,
                          UserRepository userRepository,
                          CodebaseChunkRepository codebaseChunkRepository,
                          AgentTaskLogRepository agentTaskLogRepository,
                          SecurityScanReportRepository securityScanReportRepository,
                          CodeReviewRepository codeReviewRepository,
                          TestPipelineRunRepository testPipelineRunRepository,
                          GithubRepositoryRepository githubRepositoryRepository,
                          PullRequestRepository pullRequestRepository,
                          CommitRepository commitRepository,
                          ChangedFileRepository changedFileRepository,
                          IssueRepository issueRepository,
                          EpicRepository epicRepository,
                          SprintRepository sprintRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.codebaseChunkRepository = codebaseChunkRepository;
        this.agentTaskLogRepository = agentTaskLogRepository;
        this.securityScanReportRepository = securityScanReportRepository;
        this.codeReviewRepository = codeReviewRepository;
        this.testPipelineRunRepository = testPipelineRunRepository;
        this.githubRepositoryRepository = githubRepositoryRepository;
        this.pullRequestRepository = pullRequestRepository;
        this.commitRepository = commitRepository;
        this.changedFileRepository = changedFileRepository;
        this.issueRepository = issueRepository;
        this.epicRepository = epicRepository;
        this.sprintRepository = sprintRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> getProjectsForUser(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User currentUser = userRepository.findById(principal.getId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Project> projects;
        if (currentUser.getRole() == Role.ADMIN) {
            projects = projectRepository.findAll();
        } else {
            projects = projectRepository.findAllAccessibleToUser(currentUser);
        }

        return projects.stream()
            .map(ProjectDto::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional
    public ProjectDto createProject(ProjectRequest request, Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User owner = userRepository.findById(principal.getId())
            .orElseThrow(() -> new IllegalArgumentException("Owner user not found"));

        if (projectRepository.existsByProjectKey(request.getProjectKey())) {
            throw new IllegalArgumentException("Project key '" + request.getProjectKey() + "' already exists");
        }

        Project project = new Project(
            request.getName(),
            request.getDescription(),
            request.getProjectKey(),
            owner
        );

        Project savedProject = projectRepository.save(project);

        // Assign owner as PROJECT_MANAGER member
        ProjectMember ownerMember = new ProjectMember(savedProject, owner, ProjectRole.PROJECT_MANAGER);
        projectMemberRepository.save(ownerMember);

        return ProjectDto.fromEntity(savedProject);
    }

    @Transactional
    public ProjectDto updateProject(Long id, ProjectRequest request) {
        Project project = projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        if (!project.getProjectKey().equalsIgnoreCase(request.getProjectKey()) &&
            projectRepository.existsByProjectKey(request.getProjectKey())) {
            throw new IllegalArgumentException("Project key '" + request.getProjectKey() + "' already exists");
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setProjectKey(request.getProjectKey());

        Project updatedProject = projectRepository.save(project);
        return ProjectDto.fromEntity(updatedProject);
    }

    @Transactional
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        // 1. External metadata & child embeddings (codebase_chunks, agent_task_logs, security_scan_reports)
        codebaseChunkRepository.deleteByProjectId(id);
        agentTaskLogRepository.deleteByProjectId(id);
        securityScanReportRepository.deleteByProjectId(id);

        // 2. Code reviews (cascades findings) and test pipeline runs
        codeReviewRepository.deleteByProjectId(id);
        testPipelineRunRepository.deleteByProjectId(id);

        // 3 & 4. Commits, Pull Requests (including changed files) & GitHub repository metadata
        githubRepositoryRepository.findByProjectId(id).ifPresent(githubRepo -> {
            List<PullRequest> prs = pullRequestRepository.findByGithubRepositoryId(githubRepo.getId());
            for (PullRequest pr : prs) {
                changedFileRepository.deleteByPullRequestId(pr.getId());
            }
            pullRequestRepository.deleteByGithubRepositoryId(githubRepo.getId());
            commitRepository.deleteByGithubRepositoryId(githubRepo.getId());
            githubRepositoryRepository.deleteByProjectId(id);
        });

        // 5. Backlog rankings and Issues
        issueRepository.deleteByProjectId(id);

        // 6. Sprints and Epics
        sprintRepository.deleteByProjectId(id);
        epicRepository.deleteByProjectId(id);

        // 7. Project members / memberships
        projectMemberRepository.deleteByProjectId(id);

        // 8. Delete Project entity
        projectRepository.delete(project);
    }
}
