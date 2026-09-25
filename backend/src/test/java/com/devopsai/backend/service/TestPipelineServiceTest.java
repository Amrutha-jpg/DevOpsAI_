package com.devopsai.backend.service;

import com.devopsai.backend.dto.TestPipelineRunDto;
import com.devopsai.backend.dto.TriggerPipelineRequest;
import com.devopsai.backend.entity.PipelineStatus;
import com.devopsai.backend.entity.TestPipelineRun;
import com.devopsai.backend.repository.ProjectRepository;
import com.devopsai.backend.repository.TestPipelineRunRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestPipelineServiceTest {

    @Mock
    private TestPipelineRunRepository pipelineRunRepository;

    @Mock
    private ProjectRepository projectRepository;

    private ObjectMapper objectMapper;
    private TestPipelineService testPipelineService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        testPipelineService = new TestPipelineService(pipelineRunRepository, projectRepository, objectMapper);
    }

    @Test
    void triggerPipeline_Success_ShouldReturnInitialPendingRun() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(pipelineRunRepository.save(any(TestPipelineRun.class))).thenAnswer(i -> {
            TestPipelineRun run = i.getArgument(0);
            if (run.getId() == null) run.setId(100L);
            return run;
        });

        TriggerPipelineRequest request = new TriggerPipelineRequest("feat/ci-cd", "c0de12345678");
        TestPipelineRunDto result = testPipelineService.triggerPipeline(1L, request);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(1L, result.getProjectId());
        assertEquals("c0de12345678", result.getCommitSha());
        assertEquals("feat/ci-cd", result.getBranch());
        assertEquals(PipelineStatus.PENDING, result.getStatus());
    }

    @Test
    void executePipelineAsync_Success_ShouldUpdateToPassed() {
        TestPipelineRun run = new TestPipelineRun(1L, "c0de12345678", "main");
        run.setId(100L);
        when(pipelineRunRepository.findById(100L)).thenReturn(Optional.of(run));

        testPipelineService.executePipelineAsync(100L, false);

        assertEquals(PipelineStatus.PASSED, run.getStatus());
        assertEquals(74, run.getTotalTestsCount());
        assertEquals(74, run.getPassedCount());
        assertEquals(0, run.getFailedCount());
        assertEquals(88.5, run.getCoveragePercent());
    }

    @Test
    void executePipelineAsync_SimulatedFailure_ShouldUpdateToFailed() {
        TestPipelineRun run = new TestPipelineRun(1L, "commit-fail-test", "main");
        run.setId(101L);
        when(pipelineRunRepository.findById(101L)).thenReturn(Optional.of(run));

        testPipelineService.executePipelineAsync(101L, true);

        assertEquals(PipelineStatus.FAILED, run.getStatus());
        assertEquals(2, run.getFailedCount());
        assertEquals(72, run.getPassedCount());
    }

    @Test
    void triggerPipeline_ProjectNotFound_ShouldThrowException() {
        when(projectRepository.existsById(999L)).thenReturn(false);

        TriggerPipelineRequest request = new TriggerPipelineRequest("main", "abc");
        assertThrows(IllegalArgumentException.class, () -> testPipelineService.triggerPipeline(999L, request));
    }

    @Test
    void getPipelineRunsForProject_Success() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        TestPipelineRun run = new TestPipelineRun(1L, "sha123", "main");
        run.setId(1L);
        when(pipelineRunRepository.findByProjectIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(run));

        List<TestPipelineRunDto> runs = testPipelineService.getPipelineRunsForProject(1L);
        assertNotNull(runs);
        assertEquals(1, runs.size());
        assertEquals("sha123", runs.get(0).getCommitSha());
    }

    @Test
    void getGitHubWorkflowYaml_ReturnsValidYaml() {
        String yaml = testPipelineService.getGitHubWorkflowYaml();
        assertNotNull(yaml);
        assertTrue(yaml.contains("name: DevOpsAI Automated CI/CD Pipeline"));
        assertTrue(yaml.contains("pgvector/pgvector:pg16"));
    }
}
