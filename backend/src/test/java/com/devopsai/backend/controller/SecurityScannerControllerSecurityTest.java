package com.devopsai.backend.controller;

import com.devopsai.backend.dto.SecurityFindingDto;
import com.devopsai.backend.dto.SecurityScanReportDto;
import com.devopsai.backend.dto.TriggerSecurityScanRequest;
import com.devopsai.backend.entity.SecurityRiskLevel;
import com.devopsai.backend.entity.SecurityScanCategory;
import com.devopsai.backend.entity.SecuritySeverity;
import com.devopsai.backend.security.ProjectSecurityEvaluator;
import com.devopsai.backend.service.SecurityScannerService;
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
public class SecurityScannerControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SecurityScannerService securityScannerService;

    @MockBean(name = "projectSecurity")
    private ProjectSecurityEvaluator projectSecurityEvaluator;

    @MockBean
    private com.devopsai.backend.repository.IssueRepository issueRepository;

    @Test
    void runSecurityScan_Unauthenticated_ShouldReturn401Unauthorized() throws Exception {
        TriggerSecurityScanRequest request = new TriggerSecurityScanRequest(1L, "abc12345", false);
        mockMvc.perform(post("/api/security/projects/1/scan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "dev_user", roles = {"DEVELOPER"})
    void runSecurityScan_AuthenticatedWithPermission_ShouldReturnReport() throws Exception {
        when(projectSecurityEvaluator.hasProjectPermission(eq(1L), any(), eq("DEVELOPER"))).thenReturn(true);

        SecurityScanReportDto reportDto = new SecurityScanReportDto(
                5L, 1L, "abc12345", "PASSED", SecurityRiskLevel.LOW,
                0, 0, 0, 1, 99.0,
                List.of(new SecurityFindingDto("SEC-002", "Hardcoded Fallback", "Default JWT secret",
                        SecuritySeverity.LOW, SecurityScanCategory.HARDCODED_SECRETS, "JwtTokenProvider.java", 42, "defaultSecret", "Use env var", null)),
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(securityScannerService.runSecurityScan(eq(1L), any())).thenReturn(reportDto);

        TriggerSecurityScanRequest request = new TriggerSecurityScanRequest(1L, "abc12345", false);

        mockMvc.perform(post("/api/security/projects/1/scan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.overallRiskLevel").value("LOW"))
                .andExpect(jsonPath("$.overallSecurityScore").value(99.0));
    }

    @Test
    @WithMockUser(username = "viewer_user", roles = {"VIEWER"})
    void runSecurityScan_NoDeveloperPermission_ShouldReturn403Forbidden() throws Exception {
        when(projectSecurityEvaluator.hasProjectPermission(eq(1L), any(), eq("DEVELOPER"))).thenReturn(false);

        TriggerSecurityScanRequest request = new TriggerSecurityScanRequest(1L, "abc12345", false);

        mockMvc.perform(post("/api/security/projects/1/scan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "viewer_user", roles = {"VIEWER"})
    void getLatestReportForProject_Authenticated_ShouldReturnReport() throws Exception {
        when(projectSecurityEvaluator.hasProjectPermission(eq(1L), any(), eq("VIEWER"))).thenReturn(true);

        SecurityScanReportDto reportDto = new SecurityScanReportDto(
                5L, 1L, "abc12345", "PASSED", SecurityRiskLevel.LOW,
                0, 0, 0, 1, 99.0,
                List.of(), LocalDateTime.now(), LocalDateTime.now()
        );

        when(securityScannerService.getLatestReportForProject(1L)).thenReturn(reportDto);

        mockMvc.perform(get("/api/security/projects/1/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.overallRiskLevel").value("LOW"));
    }
}
