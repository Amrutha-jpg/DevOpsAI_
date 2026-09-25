package com.devopsai.backend.controller;

import com.devopsai.backend.dto.PipelineStageDto;
import com.devopsai.backend.dto.TestPipelineRunDto;
import com.devopsai.backend.dto.TriggerPipelineRequest;
import com.devopsai.backend.entity.PipelineStatus;
import com.devopsai.backend.security.ProjectSecurityEvaluator;
import com.devopsai.backend.service.TestPipelineService;
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
public class TestPipelineControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TestPipelineService testPipelineService;

    @MockBean(name = "projectSecurity")
    private ProjectSecurityEvaluator projectSecurityEvaluator;

    @MockBean
    private com.devopsai.backend.repository.IssueRepository issueRepository;

    @Test
    void triggerPipeline_Unauthenticated_ShouldReturn401Unauthorized() throws Exception {
        TriggerPipelineRequest request = new TriggerPipelineRequest("main", "abc12345");
        mockMvc.perform(post("/api/testing/projects/1/trigger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "dev_user", roles = {"DEVELOPER"})
    void triggerPipeline_AuthenticatedWithPermission_ShouldReturnPipelineRun() throws Exception {
        when(projectSecurityEvaluator.hasProjectPermission(eq(1L), any(), eq("DEVELOPER"))).thenReturn(true);

        TestPipelineRunDto runDto = new TestPipelineRunDto(
                10L, 1L, "abc12345", "main", PipelineStatus.PASSED,
                List.of(new PipelineStageDto("BUILD", "PASSED", 450L, List.of("Build successful"))),
                74, 74, 0, 88.5, 4070L, LocalDateTime.now(), LocalDateTime.now()
        );

        when(testPipelineService.triggerPipeline(eq(1L), any())).thenReturn(runDto);

        TriggerPipelineRequest request = new TriggerPipelineRequest("main", "abc12345");

        mockMvc.perform(post("/api/testing/projects/1/trigger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.projectId").value(1));
    }

    @Test
    @WithMockUser(username = "unauthorized_user", roles = {"VIEWER"})
    void triggerPipeline_NoDeveloperPermission_ShouldReturn403Forbidden() throws Exception {
        when(projectSecurityEvaluator.hasProjectPermission(eq(1L), any(), eq("DEVELOPER"))).thenReturn(false);

        TriggerPipelineRequest request = new TriggerPipelineRequest("main", "abc12345");

        mockMvc.perform(post("/api/testing/projects/1/trigger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "dev_user", roles = {"DEVELOPER"})
    void getPipelineRunsForProject_Authenticated_ShouldReturnList() throws Exception {
        when(projectSecurityEvaluator.hasProjectPermission(eq(1L), any(), eq("VIEWER"))).thenReturn(true);

        TestPipelineRunDto runDto = new TestPipelineRunDto(
                10L, 1L, "abc12345", "main", PipelineStatus.PASSED,
                List.of(), 74, 74, 0, 88.5, 4070L, LocalDateTime.now(), LocalDateTime.now()
        );

        when(testPipelineService.getPipelineRunsForProject(1L)).thenReturn(List.of(runDto));

        mockMvc.perform(get("/api/testing/projects/1/runs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }
}
