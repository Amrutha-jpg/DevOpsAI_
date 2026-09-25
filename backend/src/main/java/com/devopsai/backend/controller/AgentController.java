package com.devopsai.backend.controller;

import com.devopsai.backend.dto.*;
import com.devopsai.backend.service.agent.AgentExecutionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    private final AgentExecutionService agentExecutionService;

    public AgentController(AgentExecutionService agentExecutionService) {
        this.agentExecutionService = agentExecutionService;
    }

    @PostMapping("/execute")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#request.projectId, authentication, 'DEVELOPER')")
    public ResponseEntity<AgentTaskResponseDto> executeAgentTask(@Valid @RequestBody AgentExecutionRequest request) {
        AgentTaskResponseDto response = agentExecutionService.executeAgentTask(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'VIEWER')")
    public ResponseEntity<List<AgentTaskResponseDto>> getTaskLogsForProject(@PathVariable Long projectId) {
        List<AgentTaskResponseDto> logs = agentExecutionService.getTaskLogsForProject(projectId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/tasks/detail/{taskId}")
    public ResponseEntity<AgentTaskResponseDto> getTaskDetail(@PathVariable Long taskId) {
        AgentTaskResponseDto detail = agentExecutionService.getTaskDetail(taskId);
        return ResponseEntity.ok(detail);
    }

    @PostMapping("/tasks/{taskId}/approve")
    public ResponseEntity<AgentTaskResponseDto> approveTask(@PathVariable Long taskId,
                                                             @RequestBody(required = false) AgentApprovalRequest request,
                                                             Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "admin";
        String feedback = request != null ? request.getFeedback() : "Approved by human oversight";
        AgentTaskResponseDto response = agentExecutionService.approveTask(taskId, username, feedback);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/tasks/{taskId}/reject")
    public ResponseEntity<AgentTaskResponseDto> rejectTask(@PathVariable Long taskId,
                                                            @RequestBody(required = false) AgentApprovalRequest request,
                                                            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "admin";
        String feedback = request != null ? request.getFeedback() : "Rejected by human oversight";
        AgentTaskResponseDto response = agentExecutionService.rejectTask(taskId, username, feedback);
        return ResponseEntity.ok(response);
    }
}
