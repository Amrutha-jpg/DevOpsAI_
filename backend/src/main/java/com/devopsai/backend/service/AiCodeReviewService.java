package com.devopsai.backend.service;

import com.devopsai.backend.dto.*;
import com.devopsai.backend.entity.*;
import com.devopsai.backend.exception.ResourceNotFoundException;
import com.devopsai.backend.repository.*;
import com.devopsai.backend.service.llm.LlmClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiCodeReviewService {

    private static final Logger logger = LoggerFactory.getLogger(AiCodeReviewService.class);

    private final CodeReviewRepository codeReviewRepository;
    private final PullRequestRepository pullRequestRepository;
    private final ChangedFileRepository changedFileRepository;
    private final ProjectRepository projectRepository;
    private final GithubRepositoryRepository githubRepositoryRepository;
    private final LlmClient llmClient;

    public AiCodeReviewService(CodeReviewRepository codeReviewRepository,
                               PullRequestRepository pullRequestRepository,
                               ChangedFileRepository changedFileRepository,
                               ProjectRepository projectRepository,
                               GithubRepositoryRepository githubRepositoryRepository,
                               LlmClient llmClient) {
        this.codeReviewRepository = codeReviewRepository;
        this.pullRequestRepository = pullRequestRepository;
        this.changedFileRepository = changedFileRepository;
        this.projectRepository = projectRepository;
        this.githubRepositoryRepository = githubRepositoryRepository;
        this.llmClient = llmClient;
    }

    @Transactional
    public CodeReviewDto initiatePullRequestReview(Long projectId, Long pullRequestId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        PullRequest pullRequest = pullRequestRepository.findById(pullRequestId)
            .orElseThrow(() -> new ResourceNotFoundException("Pull request not found with id: " + pullRequestId));

        if (pullRequest.getGithubRepository() != null && pullRequest.getGithubRepository().getProject() != null) {
            Long prProjectId = pullRequest.getGithubRepository().getProject().getId();
            if (!projectId.equals(prProjectId)) {
                throw new ResourceNotFoundException("Pull request " + pullRequestId + " does not belong to project id: " + projectId);
            }
        }

        CodeReview review = codeReviewRepository.findByPullRequestId(pullRequestId)
            .orElseGet(() -> new CodeReview(project, pullRequest));

        review.setProject(project);
        review.setPullRequest(pullRequest);
        review.setStatus(ReviewStatus.PENDING);
        review.setSummary("AI Code Review initiated for Pull Request #" + pullRequest.getNumber() + " (" + pullRequest.getTitle() + ")");
        review.getFindings().clear();

        CodeReview savedReview = codeReviewRepository.save(review);
        return new CodeReviewDto(savedReview);
    }

    @Transactional
    public CodeReviewDto initiatePullRequestReviewByNumber(Long projectId, Integer prNumber) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        GithubRepository repo = githubRepositoryRepository.findByProjectId(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("No GitHub repository connected to project id: " + projectId));

        PullRequest pullRequest = pullRequestRepository.findByGithubRepositoryIdAndNumber(repo.getId(), prNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Pull request #" + prNumber + " not found for project id: " + projectId));

        return initiatePullRequestReview(projectId, pullRequest.getId());
    }

    @Async("githubTaskExecutor")
    @Transactional
    public void analyzePullRequestAsync(Long reviewId, Long pullRequestId) {
        logger.info("Starting asynchronous AI Code Review analysis for reviewId: {}, prId: {}", reviewId, pullRequestId);

        CodeReview review = codeReviewRepository.findById(reviewId).orElse(null);
        if (review == null) {
            logger.error("Failed to execute AI review: CodeReview entity with id {} not found", reviewId);
            return;
        }

        try {
            review.setStatus(ReviewStatus.ANALYZING);
            codeReviewRepository.save(review);

            List<ChangedFile> changedFiles = changedFileRepository.findByPullRequestId(pullRequestId);
            List<CodeReviewFindingDto> aggregatedFindings = new ArrayList<>();

            if (changedFiles != null && !changedFiles.isEmpty()) {
                for (ChangedFile file : changedFiles) {
                    String fileContent = file.getPatch() != null ? file.getPatch() : file.getFilename();
                    List<CodeReviewFindingDto> findings = llmClient.analyzeCode(file.getFilename(), fileContent);
                    if (findings != null) {
                        aggregatedFindings.addAll(findings);
                    }
                }
            }

            double score = calculateQualityScore(aggregatedFindings);
            review.setQualityScore(score);
            review.setStatus(ReviewStatus.COMPLETED);
            String projectName = review.getProject() != null ? review.getProject().getName() : "Project";
            review.setSummary("AI Code Review Completed: " + aggregatedFindings.size() + " findings identified across " 
                + (changedFiles != null ? changedFiles.size() : 0) + " files for " + projectName 
                + ". Quality Score: " + String.format("%.1f", score) + "%");

            review.getFindings().clear();
            for (CodeReviewFindingDto dto : aggregatedFindings) {
                CodeReviewFinding finding = new CodeReviewFinding(
                    dto.getFilename(),
                    dto.getLineNumber(),
                    dto.getCategory() != null ? dto.getCategory() : ReviewCategory.BEST_PRACTICE,
                    dto.getSeverity() != null ? dto.getSeverity() : FindingSeverity.INFO,
                    dto.getTitle() != null ? dto.getTitle() : "Code Insight",
                    dto.getDescription(),
                    dto.getRecommendation(),
                    dto.getCodeSnippet()
                );
                review.addFinding(finding);
            }

            codeReviewRepository.save(review);
            logger.info("Successfully completed AI Code Review analysis for reviewId: {}, score: {}", reviewId, score);

        } catch (Exception e) {
            logger.error("Error during asynchronous AI Code Review analysis for reviewId: {}", reviewId, e);
            review.setStatus(ReviewStatus.FAILED);
            review.setSummary("AI Code Review failed due to internal analysis error: " + e.getMessage());
            codeReviewRepository.save(review);
        }
    }

    @Transactional
    public CodeReviewDto analyzeSnippet(AnalyzeSnippetRequest request) {
        Project project = null;
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId()).orElse(null);
        }
        if (project == null) {
            project = projectRepository.findAll().stream().findFirst().orElseThrow(() -> new ResourceNotFoundException("No project available for snippet analysis"));
        }

        CodeReview review = new CodeReview();
        review.setProject(project);
        review.setStatus(ReviewStatus.COMPLETED);

        List<CodeReviewFindingDto> findings = llmClient.analyzeCode(request.getFilename(), request.getCodeSnippet());
        double score = calculateQualityScore(findings);
        review.setQualityScore(score);
        review.setSummary("Ad-hoc snippet review completed for " + request.getFilename() + ". Quality Score: " + String.format("%.1f", score) + "%");

        for (CodeReviewFindingDto dto : findings) {
            CodeReviewFinding finding = new CodeReviewFinding(
                dto.getFilename(),
                dto.getLineNumber(),
                dto.getCategory() != null ? dto.getCategory() : ReviewCategory.BEST_PRACTICE,
                dto.getSeverity() != null ? dto.getSeverity() : FindingSeverity.INFO,
                dto.getTitle() != null ? dto.getTitle() : "Snippet Insight",
                dto.getDescription(),
                dto.getRecommendation(),
                dto.getCodeSnippet()
            );
            review.addFinding(finding);
        }

        CodeReview saved = codeReviewRepository.save(review);
        return new CodeReviewDto(saved);
    }

    @Transactional(readOnly = true)
    public CodeReviewDto getReview(Long reviewId) {
        CodeReview review = codeReviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Code review not found with id: " + reviewId));
        return new CodeReviewDto(review);
    }

    @Transactional(readOnly = true)
    public CodeReviewDto getLatestReviewForProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        List<CodeReview> reviews = codeReviewRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
        if (reviews.isEmpty()) {
            return null;
        }
        return new CodeReviewDto(reviews.get(0));
    }

    @Transactional(readOnly = true)
    public List<CodeReviewDto> getReviewsForProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        return codeReviewRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
            .map(CodeReviewDto::new)
            .collect(Collectors.toList());
    }

    public double calculateQualityScore(List<CodeReviewFindingDto> findings) {
        if (findings == null || findings.isEmpty()) {
            return 100.0;
        }

        double totalDeductions = 0;
        for (CodeReviewFindingDto f : findings) {
            FindingSeverity severity = f.getSeverity() != null ? f.getSeverity() : FindingSeverity.INFO;
            switch (severity) {
                case CRITICAL:
                    totalDeductions += 15.0;
                    break;
                case HIGH:
                    totalDeductions += 10.0;
                    break;
                case MEDIUM:
                    totalDeductions += 5.0;
                    break;
                case LOW:
                    totalDeductions += 2.0;
                    break;
                case INFO:
                default:
                    break;
            }
        }

        double score = 100.0 - totalDeductions;
        return Math.max(0.0, Math.min(100.0, score));
    }
}
