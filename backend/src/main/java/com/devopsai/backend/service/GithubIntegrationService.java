package com.devopsai.backend.service;

import com.devopsai.backend.dto.*;
import com.devopsai.backend.entity.*;
import com.devopsai.backend.exception.ResourceNotFoundException;
import com.devopsai.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GithubIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(GithubIntegrationService.class);

    private final GithubRepositoryRepository githubRepositoryRepository;
    private final CommitRepository commitRepository;
    private final PullRequestRepository pullRequestRepository;
    private final ChangedFileRepository changedFileRepository;
    private final ProjectRepository projectRepository;
    private final GithubApiClient githubApiClient;

    public GithubIntegrationService(GithubRepositoryRepository githubRepositoryRepository,
                                     CommitRepository commitRepository,
                                     PullRequestRepository pullRequestRepository,
                                     ChangedFileRepository changedFileRepository,
                                     ProjectRepository projectRepository,
                                     GithubApiClient githubApiClient) {
        this.githubRepositoryRepository = githubRepositoryRepository;
        this.commitRepository = commitRepository;
        this.pullRequestRepository = pullRequestRepository;
        this.changedFileRepository = changedFileRepository;
        this.projectRepository = projectRepository;
        this.githubApiClient = githubApiClient;
    }

    @Transactional
    public GithubRepoDto connectRepository(Long projectId, ConnectGithubRepoRequest request) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        GithubRepository repo = githubRepositoryRepository.findByProjectId(projectId)
            .orElseGet(() -> new GithubRepository(request.getOwnerName(), request.getRepoName(), request.getDefaultBranch(), request.getAccessToken(), project));

        repo.setOwnerName(request.getOwnerName());
        repo.setRepoName(request.getRepoName());
        repo.setDefaultBranch(request.getDefaultBranch() != null ? request.getDefaultBranch() : "main");
        if (request.getAccessToken() != null && !request.getAccessToken().isBlank()) {
            repo.setAccessToken(request.getAccessToken());
        }
        repo.setSyncStatus(SyncStatus.PENDING);
        repo.setHtmlUrl("https://github.com/" + request.getOwnerName() + "/" + request.getRepoName());

        GithubRepository savedRepo = githubRepositoryRepository.save(repo);
        return new GithubRepoDto(savedRepo);
    }

    @Transactional(readOnly = true)
    public GithubRepoDto getRepoForProject(Long projectId) {
        GithubRepository repo = githubRepositoryRepository.findByProjectId(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("No GitHub repository connected to project id: " + projectId));
        return new GithubRepoDto(repo);
    }

    /**
     * Executes background synchronization asynchronously using custom thread executor pool.
     */
    @Async("githubTaskExecutor")
    @Transactional
    public void syncRepositoryAsync(Long repoId) {
        logger.info("Starting asynchronous GitHub repository sync for repoId: {}", repoId);

        GithubRepository repo = githubRepositoryRepository.findById(repoId).orElse(null);
        if (repo == null) {
            logger.error("Failed to sync: GithubRepository with id {} not found", repoId);
            return;
        }

        try {
            repo.setSyncStatus(SyncStatus.IN_PROGRESS);
            githubRepositoryRepository.save(repo);

            // Fetch and persist commits
            List<Commit> commits = githubApiClient.fetchCommits(repo);
            for (Commit c : commits) {
                if (commitRepository.findByGithubRepositoryIdAndSha(repo.getId(), c.getSha()).isEmpty()) {
                    commitRepository.save(c);
                }
            }

            // Fetch and persist PRs and changed files
            List<PullRequest> prs = githubApiClient.fetchPullRequests(repo);
            for (PullRequest pr : prs) {
                PullRequest savedPr = pullRequestRepository.findByGithubRepositoryIdAndNumber(repo.getId(), pr.getNumber())
                    .map(existing -> {
                        existing.setTitle(pr.getTitle());
                        existing.setDescription(pr.getDescription());
                        existing.setState(pr.getState());
                        existing.setAdditions(pr.getAdditions());
                        existing.setDeletions(pr.getDeletions());
                        existing.setChangedFilesCount(pr.getChangedFilesCount());
                        return pullRequestRepository.save(existing);
                    })
                    .orElseGet(() -> pullRequestRepository.save(pr));

                if (changedFileRepository.findByPullRequestId(savedPr.getId()).isEmpty()) {
                    List<ChangedFile> files = githubApiClient.fetchChangedFiles(savedPr);
                    changedFileRepository.saveAll(files);
                }
            }

            repo.setSyncStatus(SyncStatus.COMPLETED);
            repo.setLastSyncedAt(LocalDateTime.now());
            githubRepositoryRepository.save(repo);
            logger.info("Successfully completed asynchronous GitHub sync for repoId: {}", repoId);
        } catch (Exception e) {
            logger.error("Error during asynchronous GitHub sync for repoId: {}", repoId, e);
            repo.setSyncStatus(SyncStatus.FAILED);
            githubRepositoryRepository.save(repo);
        }
    }

    @Transactional(readOnly = true)
    public List<CommitDto> getCommits(Long projectId) {
        GithubRepository repo = githubRepositoryRepository.findByProjectId(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("No GitHub repository connected to project id: " + projectId));

        return commitRepository.findByGithubRepositoryIdOrderByCommitDateDesc(repo.getId()).stream()
            .map(CommitDto::new)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PullRequestDto> getPullRequests(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }

        return githubRepositoryRepository.findByProjectId(projectId)
            .map(repo -> pullRequestRepository.findByGithubRepositoryIdOrderByNumberDesc(repo.getId()).stream()
                .map(PullRequestDto::new)
                .collect(Collectors.toList()))
            .orElse(java.util.Collections.emptyList());
    }

    @Transactional(readOnly = true)
    public List<ChangedFileDto> getChangedFiles(Long pullRequestId) {
        if (!pullRequestRepository.existsById(pullRequestId)) {
            throw new ResourceNotFoundException("Pull request not found with id: " + pullRequestId);
        }

        return changedFileRepository.findByPullRequestId(pullRequestId).stream()
            .map(ChangedFileDto::new)
            .collect(Collectors.toList());
    }
}
