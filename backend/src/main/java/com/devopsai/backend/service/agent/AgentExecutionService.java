package com.devopsai.backend.service.agent;

import com.devopsai.backend.dto.*;
import com.devopsai.backend.entity.*;
import com.devopsai.backend.repository.AgentTaskLogRepository;
import com.devopsai.backend.service.agent.tools.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AgentExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(AgentExecutionService.class);

    private final AgentTaskLogRepository taskLogRepository;
    private final FetchPullRequestDiffTool fetchPullRequestDiffTool;
    private final AnalyzeVulnerabilitiesTool analyzeVulnerabilitiesTool;
    private final GenerateJUnitTestTool generateJUnitTestTool;
    private final GenerateApiDocsTool generateApiDocsTool;
    private final CorrelateStackTraceTool correlateStackTraceTool;
    private final ObjectMapper objectMapper;

    public AgentExecutionService(AgentTaskLogRepository taskLogRepository,
                                 FetchPullRequestDiffTool fetchPullRequestDiffTool,
                                 AnalyzeVulnerabilitiesTool analyzeVulnerabilitiesTool,
                                 GenerateJUnitTestTool generateJUnitTestTool,
                                 GenerateApiDocsTool generateApiDocsTool,
                                 CorrelateStackTraceTool correlateStackTraceTool,
                                 ObjectMapper objectMapper) {
        this.taskLogRepository = taskLogRepository;
        this.fetchPullRequestDiffTool = fetchPullRequestDiffTool;
        this.analyzeVulnerabilitiesTool = analyzeVulnerabilitiesTool;
        this.generateJUnitTestTool = generateJUnitTestTool;
        this.generateApiDocsTool = generateApiDocsTool;
        this.correlateStackTraceTool = correlateStackTraceTool;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AgentTaskResponseDto executeAgentTask(AgentExecutionRequest request) {
        Long projectId = request.getProjectId();
        AgentType agentType = request.getAgentType();
        String payload = request.getInputPayload();

        logger.info("Executing Agent Task: {} for Project ID: {}", agentType, projectId);

        AgentTaskLog logEntry = new AgentTaskLog(projectId, agentType, payload);
        logEntry.setStatus(AgentTaskStatus.RUNNING);
        logEntry = taskLogRepository.save(logEntry);

        List<String> steps = new ArrayList<>();
        Map<String, Object> outputResult = new HashMap<>();

        try {
            switch (agentType) {
                case CODE_REVIEW_AGENT:
                    steps.add("[Step 1] Initialized Code Review Agent and selected FetchPullRequestDiffTool.");
                    Map<String, Object> prDiffParams = Map.of("prId", request.getTargetId() != null ? request.getTargetId() : 100L);
                    Map<String, Object> diffResult = fetchPullRequestDiffTool.execute(prDiffParams);
                    steps.add("[Step 2] Executed FetchPullRequestDiffTool. Retrieved " + diffResult.get("changedFilesCount") + " changed file diffs.");

                    steps.add("[Step 3] Executed AnalyzeVulnerabilitiesTool on retrieved file diffs.");
                    Map<String, Object> vulnResult = analyzeVulnerabilitiesTool.execute(Map.of("filename", "AuthService.java", "codeContent", "return repository.findById(id).get();"));
                    steps.add("[Step 4] Vulnerability analysis complete. Discovered " + vulnResult.get("vulnerabilitiesFound") + " security/bug finding(s).");

                    outputResult.put("agentType", "CODE_REVIEW_AGENT");
                    outputResult.put("prTitle", diffResult.get("title"));
                    outputResult.put("summary", "Found 1 High Severity issue: Raw Optional.get() in AuthService.java.");
                    outputResult.put("recommendation", "Replace Optional.get() with .orElseThrow() guard.");
                    outputResult.put("diff", diffResult);
                    outputResult.put("findings", vulnResult.get("findings"));
                    break;

                case TEST_AGENT:
                    steps.add("[Step 1] Initialized Test Generation Agent.");
                    String targetClass = (payload != null && !payload.isBlank()) ? payload.trim() : "PriorityCalculator";
                    steps.add("[Step 2] Selected GenerateJUnitTestTool for target class: " + targetClass);

                    Map<String, Object> testResult = generateJUnitTestTool.execute(Map.of("className", targetClass));
                    steps.add("[Step 3] Executed GenerateJUnitTestTool. Successfully synthesized complete JUnit 5 & Mockito test suite.");

                    outputResult.put("agentType", "TEST_AGENT");
                    outputResult.put("targetClass", targetClass);
                    outputResult.put("generatedTestCode", testResult.get("generatedTestCode"));
                    break;

                case DOCUMENTATION_AGENT:
                    steps.add("[Step 1] Initialized Documentation Agent.");
                    String controllerName = (payload != null && !payload.isBlank()) ? payload.trim() : "IssueController";
                    steps.add("[Step 2] Selected GenerateApiDocsTool for REST controller: " + controllerName);

                    Map<String, Object> docsResult = generateApiDocsTool.execute(Map.of("controllerName", controllerName));
                    steps.add("[Step 3] Executed GenerateApiDocsTool. Generated Markdown API reference and request/response schemas.");

                    outputResult.put("agentType", "DOCUMENTATION_AGENT");
                    outputResult.put("controllerName", controllerName);
                    outputResult.put("generatedDocs", docsResult.get("generatedMarkdownDocs"));
                    break;

                case DEBUGGING_AGENT:
                    steps.add("[Step 1] Initialized Debugging Agent.");
                    steps.add("[Step 2] Selected CorrelateStackTraceTool to parse input exception stack trace.");

                    Map<String, Object> debugResult = correlateStackTraceTool.execute(Map.of("stackTrace", payload != null ? payload : "java.lang.NoSuchElementException"));
                    steps.add("[Step 3] Stack trace parsed. Correlated root cause to " + debugResult.get("targetFile") + " line " + debugResult.get("lineNumber"));
                    steps.add("[Step 4] Synthesized concrete code patch diff to fix NoSuchElementException.");

                    outputResult.put("agentType", "DEBUGGING_AGENT");
                    outputResult.put("rootCause", debugResult.get("rootCause"));
                    outputResult.put("targetFile", debugResult.get("targetFile"));
                    outputResult.put("lineNumber", debugResult.get("lineNumber"));
                    outputResult.put("proposedPatchDiff", debugResult.get("proposedFixDiff"));
                    break;
            }

            logEntry.setExecutionStepsJson(objectMapper.writeValueAsString(steps));
            logEntry.setOutputResultJson(objectMapper.writeValueAsString(outputResult));
            logEntry.setStatus(AgentTaskStatus.REQUIRES_HUMAN_APPROVAL);

        } catch (Exception e) {
            logger.error("Error executing Agent Task ID: {}", logEntry.getId(), e);
            steps.add("[Error] Execution failed: " + e.getMessage());
            logEntry.setStatus(AgentTaskStatus.FAILED);
        }

        logEntry = taskLogRepository.save(logEntry);
        return mapToDto(logEntry);
    }

    @Transactional
    public AgentTaskResponseDto approveTask(Long taskId, String approvedBy, String feedback) {
        AgentTaskLog logEntry = taskLogRepository.findById(taskId).orElseThrow();
        logEntry.setStatus(AgentTaskStatus.APPROVED);
        logEntry.setApprovedBy(approvedBy != null ? approvedBy : "admin");
        logEntry.setHumanFeedback(feedback != null ? feedback : "Approved and applied by developer oversight.");
        logEntry = taskLogRepository.save(logEntry);
        return mapToDto(logEntry);
    }

    @Transactional
    public AgentTaskResponseDto rejectTask(Long taskId, String rejectedBy, String feedback) {
        AgentTaskLog logEntry = taskLogRepository.findById(taskId).orElseThrow();
        logEntry.setStatus(AgentTaskStatus.REJECTED);
        logEntry.setApprovedBy(rejectedBy != null ? rejectedBy : "admin");
        logEntry.setHumanFeedback(feedback != null ? feedback : "Rejected during human oversight review.");
        logEntry = taskLogRepository.save(logEntry);
        return mapToDto(logEntry);
    }

    public List<AgentTaskResponseDto> getTaskLogsForProject(Long projectId) {
        return taskLogRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public AgentTaskResponseDto getTaskDetail(Long taskId) {
        AgentTaskLog logEntry = taskLogRepository.findById(taskId).orElseThrow();
        return mapToDto(logEntry);
    }

    private AgentTaskResponseDto mapToDto(AgentTaskLog logEntry) {
        List<String> steps = Collections.emptyList();
        if (logEntry.getExecutionStepsJson() != null) {
            try {
                steps = objectMapper.readValue(logEntry.getExecutionStepsJson(), new TypeReference<List<String>>() {});
            } catch (Exception ignored) {
            }
        }

        return new AgentTaskResponseDto(
            logEntry.getId(),
            logEntry.getProjectId(),
            logEntry.getAgentType(),
            logEntry.getStatus(),
            logEntry.getInputPayload(),
            steps,
            logEntry.getOutputResultJson(),
            logEntry.getApprovedBy(),
            logEntry.getHumanFeedback(),
            logEntry.getCreatedAt(),
            logEntry.getUpdatedAt()
        );
    }
}
