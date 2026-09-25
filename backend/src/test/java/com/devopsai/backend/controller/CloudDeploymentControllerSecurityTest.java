package com.devopsai.backend.controller;

import com.devopsai.backend.dto.ContainerStatusDto;
import com.devopsai.backend.dto.DeploymentStatusDto;
import com.devopsai.backend.dto.PipelineRunDto;
import com.devopsai.backend.service.CloudDeploymentService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CloudDeploymentControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CloudDeploymentService cloudDeploymentService;

    @MockBean
    private com.devopsai.backend.repository.IssueRepository issueRepository;

    @Test
    void getDeploymentStatus_Unauthenticated_ShouldReturn401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/deployment/status"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin_user", roles = {"ADMIN"})
    void getDeploymentStatus_Authenticated_ShouldReturnDeploymentStatusDto() throws Exception {
        DeploymentStatusDto dto = new DeploymentStatusDto(
                "PRODUCTION", true, "PostgreSQL 16 Connection Active",
                true, "Redis 7 Cache Active", 256, 512, 3600, 4,
                LocalDateTime.now(), List.of(
                        new ContainerStatusDto("devopsai-backend", "eclipse-temurin:21-jre", "RUNNING", "8080:8080", "HEALTHY")
                )
        );

        when(cloudDeploymentService.getDeploymentStatus()).thenReturn(dto);

        mockMvc.perform(get("/api/deployment/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.environmentStatus").value("PRODUCTION"))
                .andExpect(jsonPath("$.dbConnected").value(true))
                .andExpect(jsonPath("$.redisHealthy").value(true))
                .andExpect(jsonPath("$.activeContainersCount").value(4));
    }

    @Test
    @WithMockUser(username = "dev_user", roles = {"DEVELOPER"})
    void triggerDeploymentPipeline_Authenticated_ShouldReturn202Accepted() throws Exception {
        PipelineRunDto run = new PipelineRunDto(
                "pipe-12345678", "GitHub Actions Deployment Pipeline",
                "a1b2c3d", "PRODUCTION", "PENDING", LocalDateTime.now(),
                null, null, "dev_user"
        );

        when(cloudDeploymentService.initiateDeploymentPipeline(any(), any())).thenReturn(run);

        mockMvc.perform(post("/api/deployment/pipeline/trigger")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetEnvironment\":\"PRODUCTION\",\"commitSha\":\"a1b2c3d\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value("pipe-12345678"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.targetEnvironment").value("PRODUCTION"));
    }
}
