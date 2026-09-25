package com.devopsai.backend.controller;

import com.devopsai.backend.dto.*;
import com.devopsai.backend.service.RagCodebaseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagCodebaseService ragCodebaseService;

    public RagController(RagCodebaseService ragCodebaseService) {
        this.ragCodebaseService = ragCodebaseService;
    }

    @PostMapping("/query")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#request.projectId, authentication, 'VIEWER')")
    public ResponseEntity<RagQueryResponse> queryCodebase(@Valid @RequestBody RagQueryRequest request) {
        RagQueryResponse response = ragCodebaseService.queryCodebase(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/index/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'PROJECT_MANAGER')")
    public ResponseEntity<CodebaseIndexStatusDto> indexProjectCodebase(@PathVariable Long projectId) {
        CodebaseIndexStatusDto status = ragCodebaseService.indexProjectCodebase(projectId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/status/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'VIEWER')")
    public ResponseEntity<CodebaseIndexStatusDto> getIndexStatus(@PathVariable Long projectId) {
        CodebaseIndexStatusDto status = ragCodebaseService.getIndexStatus(projectId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/suggested-questions/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.hasProjectPermission(#projectId, authentication, 'VIEWER')")
    public ResponseEntity<List<String>> getSuggestedQuestions(@PathVariable Long projectId) {
        List<String> questions = ragCodebaseService.getSuggestedQuestions(projectId);
        return ResponseEntity.ok(questions);
    }
}
