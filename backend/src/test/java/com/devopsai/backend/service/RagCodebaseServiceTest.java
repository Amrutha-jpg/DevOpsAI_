package com.devopsai.backend.service;

import com.devopsai.backend.dto.CodebaseIndexStatusDto;
import com.devopsai.backend.dto.RagQueryRequest;
import com.devopsai.backend.dto.RagQueryResponse;
import com.devopsai.backend.entity.Project;
import com.devopsai.backend.entity.Role;
import com.devopsai.backend.entity.User;
import com.devopsai.backend.repository.CommitRepository;
import com.devopsai.backend.repository.GithubRepositoryRepository;
import com.devopsai.backend.repository.ProjectRepository;
import com.devopsai.backend.service.llm.LlmClient;
import com.devopsai.backend.service.rag.DefaultEmbeddingClient;
import com.devopsai.backend.service.rag.EmbeddingClient;
import com.devopsai.backend.service.rag.InMemoryVectorStore;
import com.devopsai.backend.service.rag.VectorStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RagCodebaseServiceTest {

    private VectorStore vectorStore;
    private EmbeddingClient embeddingClient;

    @Mock
    private LlmClient llmClient;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private GithubRepositoryRepository githubRepositoryRepository;

    @Mock
    private CommitRepository commitRepository;

    private ObjectMapper objectMapper;
    private RagCodebaseService ragCodebaseService;

    private Project smartHealthProject;
    private Project devOpsAiProject;

    @BeforeEach
    void setUp() {
        vectorStore = new InMemoryVectorStore();
        embeddingClient = new DefaultEmbeddingClient();
        objectMapper = new ObjectMapper();

        ragCodebaseService = new RagCodebaseService(
            vectorStore,
            embeddingClient,
            llmClient,
            projectRepository,
            githubRepositoryRepository,
            commitRepository,
            objectMapper
        );

        User owner = new User("admin", "admin@devopsai.io", "password", Role.ADMIN);
        smartHealthProject = new Project("SmartHealth Platform", "Health platform app", "SMART", owner);
        smartHealthProject.setId(1L);

        devOpsAiProject = new Project("DevOpsAI Platform", "DevOps platform app", "DEVOPS", owner);
        devOpsAiProject.setId(2L);
    }

    @Test
    void indexProjectCodebase_ShouldChunkAndEmbedSourceCode() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(smartHealthProject));
        when(githubRepositoryRepository.findByProjectId(1L)).thenReturn(Optional.empty());

        CodebaseIndexStatusDto status = ragCodebaseService.indexProjectCodebase(1L);

        assertNotNull(status);
        assertEquals(1L, status.getProjectId());
        assertTrue(status.getTotalChunks() > 0);
        assertTrue(status.getIndexedFilesCount() > 0);
        assertNotNull(status.getLastIndexedAt());
    }

    @Test
    void queryCodebase_SmartHealthProject_ShouldReturnIsolatedSmartHealthChunks() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(smartHealthProject));
        when(githubRepositoryRepository.findByProjectId(1L)).thenReturn(Optional.empty());
        when(llmClient.generateCompletion(anyString()))
            .thenReturn("Patient sensor telemetry validation is handled in TelemetryService.java: lines 15–30.");

        RagQueryRequest request = new RagQueryRequest(1L, "Where is telemetry validation handled?", 3);
        RagQueryResponse response = ragCodebaseService.queryCodebase(request);

        assertNotNull(response);
        assertNotNull(response.getAnswer());
        assertTrue(response.getAnswer().contains("TelemetryService"));
        assertFalse(response.getSources().isEmpty());
        // Verify vector isolation: sources MUST contain TelemetryService.java, NOT SecurityConfig or PriorityCalculator
        assertTrue(response.getSources().stream().anyMatch(s -> s.getFilename().contains("TelemetryService.java")));
        assertFalse(response.getSources().stream().anyMatch(s -> s.getFilename().contains("PriorityCalculator.java")));
    }

    @Test
    void queryCodebase_DevOpsAiProject_ShouldReturnIsolatedDevOpsAiChunks() {
        when(projectRepository.findById(2L)).thenReturn(Optional.of(devOpsAiProject));
        when(githubRepositoryRepository.findByProjectId(2L)).thenReturn(Optional.empty());
        when(llmClient.generateCompletion(anyString()))
            .thenReturn("Authentication is handled in SecurityConfig.java.");

        RagQueryRequest request = new RagQueryRequest(2L, "Where is auth handled?", 3);
        RagQueryResponse response = ragCodebaseService.queryCodebase(request);

        assertNotNull(response);
        assertNotNull(response.getAnswer());
        assertFalse(response.getSources().isEmpty());
        // Verify vector isolation: sources MUST contain SecurityConfig.java, NOT TelemetryService
        assertTrue(response.getSources().stream().anyMatch(s -> s.getFilename().contains("SecurityConfig.java")));
        assertFalse(response.getSources().stream().anyMatch(s -> s.getFilename().contains("TelemetryService.java")));
    }

    @Test
    void getSuggestedQuestions_ShouldReturnProjectSpecificQuestions() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(smartHealthProject));

        var questions = ragCodebaseService.getSuggestedQuestions(1L);
        assertNotNull(questions);
        assertEquals(4, questions.size());
        assertTrue(questions.get(0).contains("telemetry"));
    }
}
