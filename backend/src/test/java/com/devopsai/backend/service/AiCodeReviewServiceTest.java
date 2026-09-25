package com.devopsai.backend.service;

import com.devopsai.backend.dto.AnalyzeSnippetRequest;
import com.devopsai.backend.dto.CodeReviewDto;
import com.devopsai.backend.dto.CodeReviewFindingDto;
import com.devopsai.backend.entity.*;
import com.devopsai.backend.repository.*;
import com.devopsai.backend.service.llm.LlmClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiCodeReviewServiceTest {

    @Mock
    private CodeReviewRepository codeReviewRepository;

    @Mock
    private CodeReviewFindingRepository codeReviewFindingRepository;

    @Mock
    private PullRequestRepository pullRequestRepository;

    @Mock
    private ChangedFileRepository changedFileRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private LlmClient llmClient;

    @InjectMocks
    private AiCodeReviewService aiCodeReviewService;

    private Project testProject;
    private PullRequest testPr;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("dev_user", "dev@devopsai.io", "password", Role.DEVELOPER);
        testUser.setId(1L);

        testProject = new Project("SmartHealth", "Telemetry Platform", "SMART", testUser);
        testProject.setId(10L);

        testPr = new PullRequest(42, "Add Telemetry", "PR description", PullRequestState.OPEN, "feature", "main", "dev_user", 1, 10, 2, null);
        testPr.setId(100L);
    }

    @Test
    @DisplayName("Should correctly calculate Code Quality Score based on severity deductions")
    void testCalculateQualityScore() {
        CodeReviewFindingDto f1 = new CodeReviewFindingDto("A.java", 1, ReviewCategory.BUG, FindingSeverity.CRITICAL, "Critical Bug", "Desc", "Fix", "code");
        CodeReviewFindingDto f2 = new CodeReviewFindingDto("B.java", 2, ReviewCategory.SECURITY, FindingSeverity.HIGH, "High Vulnerability", "Desc", "Fix", "code");
        CodeReviewFindingDto f3 = new CodeReviewFindingDto("C.java", 3, ReviewCategory.PERFORMANCE, FindingSeverity.MEDIUM, "Perf issue", "Desc", "Fix", "code");

        // Deductions: CRITICAL (15) + HIGH (10) + MEDIUM (5) = 30 -> Score = 70.0
        double score = aiCodeReviewService.calculateQualityScore(List.of(f1, f2, f3));

        assertEquals(70.0, score, 0.001);
    }

    @Test
    @DisplayName("Should initiate pull request review with PENDING status")
    void testInitiatePullRequestReviewSuccess() {
        when(projectRepository.findById(10L)).thenReturn(Optional.of(testProject));
        when(pullRequestRepository.findById(100L)).thenReturn(Optional.of(testPr));
        when(codeReviewRepository.findByPullRequestId(100L)).thenReturn(Optional.empty());
        when(codeReviewRepository.save(any(CodeReview.class))).thenAnswer(i -> {
            CodeReview cr = i.getArgument(0);
            cr.setId(500L);
            return cr;
        });

        CodeReviewDto dto = aiCodeReviewService.initiatePullRequestReview(10L, 100L);

        assertNotNull(dto);
        assertEquals(500L, dto.getId());
        assertEquals(ReviewStatus.PENDING, dto.getStatus());
        verify(codeReviewRepository, times(1)).save(any(CodeReview.class));
    }

    @Test
    @DisplayName("Should execute analyzePullRequestAsync and transition status from ANALYZING to COMPLETED")
    void testAnalyzePullRequestAsync() {
        CodeReview review = new CodeReview(testProject, testPr);
        review.setId(500L);
        review.setStatus(ReviewStatus.PENDING);

        ChangedFile file = new ChangedFile("TelemetryService.java", "modified", 10, 2, "public User getUser(Long id) { return repo.findById(id).get(); }", testPr);

        when(codeReviewRepository.findById(500L)).thenReturn(Optional.of(review));
        when(changedFileRepository.findByPullRequestId(100L)).thenReturn(List.of(file));
        when(llmClient.analyzeCode(anyString(), anyString())).thenReturn(List.of(
            new CodeReviewFindingDto("TelemetryService.java", 1, ReviewCategory.BUG, FindingSeverity.HIGH, "Optional.get() Bug", "Desc", "Use orElseThrow", "repo.findById(id).get()")
        ));

        aiCodeReviewService.analyzePullRequestAsync(500L, 100L);

        assertEquals(ReviewStatus.COMPLETED, review.getStatus());
        assertEquals(90.0, review.getQualityScore()); // 100 - HIGH(10) = 90
        verify(codeReviewRepository, atLeastOnce()).save(review);
    }

    @Test
    @DisplayName("Should analyze ad-hoc code snippet for developer sandbox")
    void testAnalyzeSnippet() {
        AnalyzeSnippetRequest req = new AnalyzeSnippetRequest("Snippet.java", "public void test() {}", 10L);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(testProject));
        when(llmClient.analyzeCode(anyString(), anyString())).thenReturn(List.of(
            new CodeReviewFindingDto("Snippet.java", 1, ReviewCategory.BEST_PRACTICE, FindingSeverity.INFO, "Clean Snippet", "Desc", "Rec", "code")
        ));
        when(codeReviewRepository.save(any(CodeReview.class))).thenAnswer(i -> {
            CodeReview cr = i.getArgument(0);
            cr.setId(800L);
            return cr;
        });

        CodeReviewDto result = aiCodeReviewService.analyzeSnippet(req);

        assertNotNull(result);
        assertEquals(800L, result.getId());
        assertEquals(ReviewStatus.COMPLETED, result.getStatus());
        assertEquals(100.0, result.getQualityScore());
    }
}
