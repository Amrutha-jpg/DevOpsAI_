package com.devopsai.backend.service;

import com.devopsai.backend.dto.SecurityScanReportDto;
import com.devopsai.backend.dto.TriggerSecurityScanRequest;
import com.devopsai.backend.entity.GithubRepository;
import com.devopsai.backend.entity.Project;
import com.devopsai.backend.entity.SecurityRiskLevel;
import com.devopsai.backend.entity.SecurityScanReport;
import com.devopsai.backend.exception.ResourceNotFoundException;
import com.devopsai.backend.repository.GithubRepositoryRepository;
import com.devopsai.backend.repository.ProjectRepository;
import com.devopsai.backend.repository.SecurityScanReportRepository;
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
public class SecurityScannerServiceTest {

    @Mock
    private SecurityScanReportRepository reportRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private GithubRepositoryRepository githubRepositoryRepository;

    @Mock
    private GithubApiClient githubApiClient;

    private ObjectMapper objectMapper;
    private SecurityScannerService securityScannerService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        securityScannerService = new SecurityScannerService(
                reportRepository, projectRepository, githubRepositoryRepository, githubApiClient, objectMapper);
    }

    @Test
    void runSecurityScan_Baseline_ShouldReturnLowRiskAndHighScore() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        GithubRepository repo = new GithubRepository("owner", "repo", "main", "ghp_mocktoken123", new Project());
        when(githubRepositoryRepository.findByProjectId(1L)).thenReturn(Optional.of(repo));

        when(reportRepository.save(any(SecurityScanReport.class))).thenAnswer(i -> {
            SecurityScanReport r = i.getArgument(0);
            if (r.getId() == null) r.setId(10L);
            return r;
        });

        TriggerSecurityScanRequest request = new TriggerSecurityScanRequest(1L, "sha123", false);
        SecurityScanReportDto report = securityScannerService.runSecurityScan(1L, request);

        assertNotNull(report);
        assertEquals(10L, report.getId());
        assertEquals(SecurityRiskLevel.LOW, report.getOverallRiskLevel());
        assertTrue(report.getOverallSecurityScore() >= 90.0);
        assertEquals(0, report.getCriticalCount());
        assertEquals(0, report.getHighCount());
    }

    @Test
    void runSecurityScan_SimulatedVulnerabilities_ShouldCalculateCriticalRiskLevel() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        GithubRepository repo = new GithubRepository("owner", "repo", "main", "ghp_mocktoken123", new Project());
        when(githubRepositoryRepository.findByProjectId(1L)).thenReturn(Optional.of(repo));

        when(reportRepository.save(any(SecurityScanReport.class))).thenAnswer(i -> {
            SecurityScanReport r = i.getArgument(0);
            if (r.getId() == null) r.setId(11L);
            return r;
        });

        TriggerSecurityScanRequest request = new TriggerSecurityScanRequest(1L, "commit-vuln-test", true);
        SecurityScanReportDto report = securityScannerService.runSecurityScan(1L, request);

        assertNotNull(report);
        assertEquals("FAILED", report.getStatus());
        assertTrue(report.getCriticalCount() >= 2);
        assertEquals(SecurityRiskLevel.CRITICAL, report.getOverallRiskLevel());
        assertTrue(report.getOverallSecurityScore() < 50.0);
        assertFalse(report.getFindings().isEmpty());
    }

    @Test
    void runSecurityScan_NoLinkedGithubRepo_ShouldThrowException() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(githubRepositoryRepository.findByProjectId(1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                securityScannerService.runSecurityScan(1L, new TriggerSecurityScanRequest(1L, "head", false)));
        assertEquals("Repository not connected for project: 1", ex.getMessage());
    }

    @Test
    void runSecurityScan_ProjectNotFound_ShouldThrowException() {
        when(projectRepository.existsById(999L)).thenReturn(false);
        assertThrows(RuntimeException.class, () ->
                securityScannerService.runSecurityScan(999L, new TriggerSecurityScanRequest(999L, "head", false)));
    }

    @Test
    void getReportsForProject_Success() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        SecurityScanReport report = new SecurityScanReport(1L, "sha123");
        report.setId(1L);
        report.setOverallRiskLevel(SecurityRiskLevel.LOW);
        when(reportRepository.findByProjectIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(report));

        List<SecurityScanReportDto> reports = securityScannerService.getReportsForProject(1L);
        assertNotNull(reports);
        assertEquals(1, reports.size());
        assertEquals(SecurityRiskLevel.LOW, reports.get(0).getOverallRiskLevel());
    }
}
