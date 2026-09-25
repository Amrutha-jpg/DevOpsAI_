package com.devopsai.backend.controller;

import com.devopsai.backend.dto.AgentExecutionRequest;
import com.devopsai.backend.dto.AgentTaskResponseDto;
import com.devopsai.backend.entity.AgentTaskStatus;
import com.devopsai.backend.entity.AgentType;
import com.devopsai.backend.security.ProjectSecurityEvaluator;
import com.devopsai.backend.service.agent.AgentExecutionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AgentControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AgentExecutionService agentExecutionService;

    @MockBean(name = "projectSecurity")
    private ProjectSecurityEvaluator projectSecurityEvaluator;

    @MockBean
    private com.devopsai.backend.repository.IssueRepository issueRepository;

    @Test
    void executeAgentTask_Unauthenticated_ShouldReturn401Unauthorized() throws Exception {
        AgentExecutionRequest request = new AgentExecutionRequest(1L, AgentType.DEBUGGING_AGENT, null, "NoSuchElementException");
        mockMvc.perform(post("/api/agents/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "dev_user", roles = {"DEVELOPER"})
    void executeAgentTask_Authenticated_ShouldReturnAgentTaskResponse() throws Exception {
        when(projectSecurityEvaluator.hasProjectPermission(eq(1L), any(), eq("DEVELOPER"))).thenReturn(true);

        AgentTaskResponseDto response = new AgentTaskResponseDto(
            1L, 1L, AgentType.DEBUGGING_AGENT, AgentTaskStatus.REQUIRES_HUMAN_APPROVAL,
            "NoSuchElementException", List.of("Parsed stack trace", "Generated fix"),
            "{\"patch\":\"diff\"}", null, null, LocalDateTime.now(), LocalDateTime.now()
        );

        when(agentExecutionService.executeAgentTask(any(AgentExecutionRequest.class))).thenReturn(response);

        AgentExecutionRequest request = new AgentExecutionRequest(1L, AgentType.DEBUGGING_AGENT, null, "NoSuchElementException");

        mockMvc.perform(post("/api/agents/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(1))
                .andExpect(jsonPath("$.agentType").value("DEBUGGING_AGENT"))
                .andExpect(jsonPath("$.status").value("REQUIRES_HUMAN_APPROVAL"));
    }

    @Test
    @WithMockUser(username = "dev_user", roles = {"DEVELOPER"})
    void getTaskLogsForProject_Authenticated_ShouldReturnList() throws Exception {
        when(projectSecurityEvaluator.hasProjectPermission(eq(1L), any(), eq("VIEWER"))).thenReturn(true);

        AgentTaskResponseDto response = new AgentTaskResponseDto(
            1L, 1L, AgentType.TEST_AGENT, AgentTaskStatus.APPROVED,
            "PriorityCalculator", List.of("Generated test"),
            "{\"code\":\"class PriorityCalculatorTest {}\"}", "dev_user", "Approved", LocalDateTime.now(), LocalDateTime.now()
        );

        when(agentExecutionService.getTaskLogsForProject(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/agents/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].taskId").value(1))
                .andExpect(jsonPath("$[0].agentType").value("TEST_AGENT"));
    }
}
