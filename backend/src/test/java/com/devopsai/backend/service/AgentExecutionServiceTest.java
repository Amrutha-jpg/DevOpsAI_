package com.devopsai.backend.service;

import com.devopsai.backend.dto.AgentExecutionRequest;
import com.devopsai.backend.dto.AgentTaskResponseDto;
import com.devopsai.backend.entity.AgentTaskLog;
import com.devopsai.backend.entity.AgentTaskStatus;
import com.devopsai.backend.entity.AgentType;
import com.devopsai.backend.repository.AgentTaskLogRepository;
import com.devopsai.backend.repository.PullRequestRepository;
import com.devopsai.backend.service.agent.AgentExecutionService;
import com.devopsai.backend.service.agent.tools.*;
import com.devopsai.backend.service.llm.FallbackRuleEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AgentExecutionServiceTest {

    @Mock
    private AgentTaskLogRepository taskLogRepository;

    @Mock
    private PullRequestRepository pullRequestRepository;

    @Mock
    private com.devopsai.backend.repository.ChangedFileRepository changedFileRepository;

    private FetchPullRequestDiffTool fetchPullRequestDiffTool;
    private AnalyzeVulnerabilitiesTool analyzeVulnerabilitiesTool;
    private GenerateJUnitTestTool generateJUnitTestTool;
    private GenerateApiDocsTool generateApiDocsTool;
    private CorrelateStackTraceTool correlateStackTraceTool;
    private ObjectMapper objectMapper;

    private AgentExecutionService agentExecutionService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        fetchPullRequestDiffTool = new FetchPullRequestDiffTool(pullRequestRepository, changedFileRepository);
        analyzeVulnerabilitiesTool = new AnalyzeVulnerabilitiesTool(new FallbackRuleEngine());
        generateJUnitTestTool = new GenerateJUnitTestTool();
        generateApiDocsTool = new GenerateApiDocsTool();
        correlateStackTraceTool = new CorrelateStackTraceTool();

        agentExecutionService = new AgentExecutionService(
            taskLogRepository,
            fetchPullRequestDiffTool,
            analyzeVulnerabilitiesTool,
            generateJUnitTestTool,
            generateApiDocsTool,
            correlateStackTraceTool,
            objectMapper
        );
    }

    @Test
    void executeAgentTask_DebuggingAgent_ShouldCorrelateStackTraceAndProposeFix() {
        when(taskLogRepository.save(any(AgentTaskLog.class))).thenAnswer(i -> {
            AgentTaskLog log = i.getArgument(0);
            if (log.getId() == null) log.setId(10L);
            return log;
        });

        AgentExecutionRequest request = new AgentExecutionRequest(
            1L,
            AgentType.DEBUGGING_AGENT,
            null,
            "java.lang.NoSuchElementException: No value present at TelemetryService.java:35"
        );

        AgentTaskResponseDto response = agentExecutionService.executeAgentTask(request);

        assertNotNull(response);
        assertEquals(10L, response.getTaskId());
        assertEquals(AgentTaskStatus.REQUIRES_HUMAN_APPROVAL, response.getStatus());
        assertFalse(response.getExecutionSteps().isEmpty());
        assertTrue(response.getOutputResult().contains("proposedPatchDiff"));
    }

    @Test
    void approveTask_ShouldUpdateStatusToApproved() {
        AgentTaskLog log = new AgentTaskLog(1L, AgentType.TEST_AGENT, "PriorityCalculator");
        log.setId(20L);
        log.setStatus(AgentTaskStatus.REQUIRES_HUMAN_APPROVAL);

        when(taskLogRepository.findById(20L)).thenReturn(Optional.of(log));
        when(taskLogRepository.save(any(AgentTaskLog.class))).thenAnswer(i -> i.getArgument(0));

        AgentTaskResponseDto response = agentExecutionService.approveTask(20L, "dev_user", "Looks good, applying JUnit test.");

        assertNotNull(response);
        assertEquals(AgentTaskStatus.APPROVED, response.getStatus());
        assertEquals("dev_user", response.getApprovedBy());
        assertTrue(response.getHumanFeedback().contains("Looks good"));
    }
}
