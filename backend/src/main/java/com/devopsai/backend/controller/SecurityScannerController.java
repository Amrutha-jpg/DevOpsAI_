package com.devopsai.backend.controller;

import com.devopsai.backend.dto.SecurityScanReportDto;
import com.devopsai.backend.dto.TriggerSecurityScanRequest;
import com.devopsai.backend.service.SecurityScannerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/security")
public class SecurityScannerController {

    private final SecurityScannerService securityScannerService;

    public SecurityScannerController(SecurityScannerService securityScannerService) {
        this.securityScannerService = securityScannerService;
    }

    @PostMapping("/projects/{projectId}/scan")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'DEVELOPER')")
    public ResponseEntity<SecurityScanReportDto> runSecurityScan(@PathVariable Long projectId,
                                                                 @RequestBody(required = false) TriggerSecurityScanRequest request) {
        SecurityScanReportDto report = securityScannerService.runSecurityScan(projectId, request);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/projects/{projectId}/reports")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'VIEWER')")
    public ResponseEntity<List<SecurityScanReportDto>> getReportsForProject(@PathVariable Long projectId) {
        List<SecurityScanReportDto> reports = securityScannerService.getReportsForProject(projectId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/projects/{projectId}/latest")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'VIEWER')")
    public ResponseEntity<SecurityScanReportDto> getLatestReportForProject(@PathVariable Long projectId) {
        SecurityScanReportDto report = securityScannerService.getLatestReportForProject(projectId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/reports/{reportId}")
    public ResponseEntity<SecurityScanReportDto> getReportById(@PathVariable Long reportId) {
        SecurityScanReportDto report = securityScannerService.getReportById(reportId);
        return ResponseEntity.ok(report);
    }
}
