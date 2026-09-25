package com.devopsai.backend.service;

import com.devopsai.backend.dto.PipelineStageDto;
import com.devopsai.backend.dto.TestPipelineRunDto;
import com.devopsai.backend.dto.TriggerPipelineRequest;
import com.devopsai.backend.entity.PipelineStatus;
import com.devopsai.backend.entity.TestPipelineRun;
import com.devopsai.backend.repository.ProjectRepository;
import com.devopsai.backend.repository.TestPipelineRunRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TestPipelineService {

    private final TestPipelineRunRepository pipelineRunRepository;
    private final ProjectRepository projectRepository;
    private final ObjectMapper objectMapper;

    public TestPipelineService(TestPipelineRunRepository pipelineRunRepository,
                               ProjectRepository projectRepository,
                               ObjectMapper objectMapper) {
        this.pipelineRunRepository = pipelineRunRepository;
        this.projectRepository = projectRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public TestPipelineRunDto triggerPipeline(Long projectId, TriggerPipelineRequest request) {
        if (!projectRepository.existsById(projectId)) {
            throw new IllegalArgumentException("Project not found with id: " + projectId);
        }

        String commitSha = (request != null && request.getCommitSha() != null && !request.getCommitSha().isBlank())
                ? request.getCommitSha() : "a1b2c3d4e5f6";
        String branch = (request != null && request.getBranch() != null && !request.getBranch().isBlank())
                ? request.getBranch() : "main";

        TestPipelineRun run = new TestPipelineRun(projectId, commitSha, branch);
        run.setStatus(PipelineStatus.PENDING);
        run.setStageResultsJson("[]");

        TestPipelineRun savedRun = pipelineRunRepository.save(run);

        boolean simulateFailure = commitSha.toLowerCase().contains("fail");
        
        // Asynchronously execute pipeline steps
        executePipelineAsync(savedRun.getId(), simulateFailure);

        return toDto(savedRun);
    }

    @Async
    @Transactional
    public void executePipelineAsync(Long runId, boolean simulateFailure) {
        TestPipelineRun run = pipelineRunRepository.findById(runId).orElse(null);
        if (run == null) return;

        try {
            // Stage 1: BUILDING
            run.setStatus(PipelineStatus.BUILDING);
            pipelineRunRepository.save(run);

            List<PipelineStageDto> stages = buildStageResults(simulateFailure);

            // Stage 2: TESTING
            run.setStatus(PipelineStatus.TESTING);
            long totalDuration = stages.stream().mapToLong(PipelineStageDto::getDurationMs).sum();
            int totalTests = 74;
            int failedCount = simulateFailure ? 2 : 0;
            int passedCount = totalTests - failedCount;
            double coverage = simulateFailure ? 72.4 : 88.5;

            run.setTotalTestsCount(totalTests);
            run.setPassedCount(passedCount);
            run.setFailedCount(failedCount);
            run.setCoveragePercent(coverage);
            run.setDurationMs(totalDuration);
            run.setStageResultsJson(objectMapper.writeValueAsString(stages));

            // Terminal status
            run.setStatus(simulateFailure ? PipelineStatus.FAILED : PipelineStatus.PASSED);
            pipelineRunRepository.save(run);
        } catch (JsonProcessingException e) {
            run.setStatus(PipelineStatus.FAILED);
            pipelineRunRepository.save(run);
        }
    }

    @Transactional(readOnly = true)
    public List<TestPipelineRunDto> getPipelineRunsForProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new IllegalArgumentException("Project not found with id: " + projectId);
        }
        return pipelineRunRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TestPipelineRunDto getPipelineRun(Long runId) {
        TestPipelineRun run = pipelineRunRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("Pipeline run not found with id: " + runId));
        return toDto(run);
    }

    public String getGitHubWorkflowYaml() {
        return """
                name: DevOpsAI Automated CI/CD Pipeline

                on:
                  push:
                    branches: [ main, develop ]
                  pull_request:
                    branches: [ main ]

                jobs:
                  build-and-test:
                    runs-on: ubuntu-latest

                    services:
                      postgres:
                        image: pgvector/pgvector:pg16
                        env:
                          POSTGRES_DB: devopsai_test
                          POSTGRES_USER: devopsai
                          POSTGRES_PASSWORD: devopsaipassword
                        ports:
                          - 5432:5432
                        options: >-
                          --health-cmd pg_isready
                          --health-interval 10s
                          --health-timeout 5s
                          --health-retries 5

                    steps:
                      - name: Checkout Repository
                        uses: actions/checkout@v4

                      - name: Set up JDK 24
                        uses: actions/setup-java@v4
                        with:
                          java-version: '24'
                          distribution: 'temurin'

                      - name: Build Backend with Maven
                        run: ./mvnw clean compile -DskipTests
                        working-directory: ./backend

                      - name: Run Unit Tests
                        run: ./mvnw test -Dtest=*Test
                        working-directory: ./backend

                      - name: Run Integration Tests
                        run: ./mvnw verify -Dtest=*IntegrationTest
                        working-directory: ./backend

                      - name: Run API Contract Tests
                        run: |
                          npm install -g newman
                          newman run ./postman/devopsai_api_contract.json

                      - name: Run AI Security Vulnerability Scan
                        run: ./mvnw spotbugs:check checkstyle:check
                        working-directory: ./backend
                """;
    }

    private List<PipelineStageDto> buildStageResults(boolean simulateFailure) {
        List<PipelineStageDto> stages = new ArrayList<>();

        // Stage 1: Build
        stages.add(new PipelineStageDto(
                "BUILD",
                "PASSED",
                450L,
                Arrays.asList(
                        "[INFO] Initializing DevOpsAI Maven Build Task...",
                        "[INFO] Compiling 68 Spring Boot Java 24 source files...",
                        "[INFO] Compiling React Vite 5 frontend assets with Tailwind CSS...",
                        "[INFO] Build artifact generated successfully: backend-1.0.0.jar (34.2 MB)"
                )
        ));

        // Stage 2: Unit Tests
        stages.add(new PipelineStageDto(
                "UNIT_TESTS",
                simulateFailure ? "FAILED" : "PASSED",
                820L,
                simulateFailure ? Arrays.asList(
                        "[INFO] Running JUnit 5 Core Engine tests...",
                        "[INFO] Executed 44 unit tests across 12 test suites",
                        "[ERROR] TestPriorityCalculatorTest.testHeapInversion() FAILED - Expected priority score 9.2 but got 8.1",
                        "[ERROR] Passed: 42, Failed: 2, Skipped: 0"
                ) : Arrays.asList(
                        "[INFO] Running JUnit 5 Core Engine tests...",
                        "[INFO] Executed 44 unit tests across 12 test suites",
                        "[INFO] TestAuthService, TestPriorityCalculator, TestRagCodebase, TestAgents: PASSED",
                        "[INFO] Passed: 44, Failed: 0, Skipped: 0"
                )
        ));

        // Stage 3: Integration Tests
        stages.add(new PipelineStageDto(
                "INTEGRATION_TESTS",
                "PASSED",
                1200L,
                Arrays.asList(
                        "[INFO] Bootstrapping SpringBootTest application context with H2 / PgVector DB...",
                        "[INFO] Running 12 Spring MockMvc REST API integration tests...",
                        "[INFO] Verified GitHub Webhook signature validation, JWT RBAC security headers, and AI Agent Execution loops",
                        "[INFO] Passed: 12, Failed: 0"
                )
        ));

        // Stage 4: API Contract Tests
        stages.add(new PipelineStageDto(
                "API_CONTRACT_TESTS",
                "PASSED",
                650L,
                Arrays.asList(
                        "[INFO] Executing OpenAPI 3.0 & Postman Contract Test Suite...",
                        "[INFO] GET /api/auth/me - HTTP 200 OK (Schema Validated)",
                        "[INFO] POST /api/projects - HTTP 201 Created (Schema Validated)",
                        "[INFO] POST /api/rag/query - HTTP 200 OK (Vector Search Schema Validated)",
                        "[INFO] POST /api/agent/execute - HTTP 200 OK (Agent Task Schema Validated)",
                        "[INFO] All 18 API Contract Assertions PASSED"
                )
        ));

        // Stage 5: AI Security Scan
        stages.add(new PipelineStageDto(
                "AI_SECURITY_SCAN",
                "PASSED",
                950L,
                Arrays.asList(
                        "[INFO] Initializing DevOpsAI Security Scanner & SAST Analyzer...",
                        "[INFO] Scanning codebase AST for SQL Injection, Reflected XSS, Hardcoded Secrets, and Unsafe Invocations...",
                        "[INFO] Inspecting `@PreAuthorize` project scoping across all REST Controllers...",
                        "[INFO] 0 High, 0 Medium, 0 Low Vulnerabilities detected. Security Score: 100/100"
                )
        ));

        return stages;
    }

    public TestPipelineRunDto toDto(TestPipelineRun run) {
        List<PipelineStageDto> stages;
        try {
            if (run.getStageResultsJson() != null && !run.getStageResultsJson().isBlank()) {
                stages = objectMapper.readValue(run.getStageResultsJson(), new TypeReference<List<PipelineStageDto>>() {});
            } else {
                stages = new ArrayList<>();
            }
        } catch (Exception e) {
            stages = new ArrayList<>();
        }

        return new TestPipelineRunDto(
                run.getId(),
                run.getProjectId(),
                run.getCommitSha(),
                run.getBranch(),
                run.getStatus(),
                stages,
                run.getTotalTestsCount(),
                run.getPassedCount(),
                run.getFailedCount(),
                run.getCoveragePercent(),
                run.getDurationMs(),
                run.getCreatedAt(),
                run.getUpdatedAt()
        );
    }
}
