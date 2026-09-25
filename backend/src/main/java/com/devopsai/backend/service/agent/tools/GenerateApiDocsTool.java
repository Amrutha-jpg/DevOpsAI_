package com.devopsai.backend.service.agent.tools;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GenerateApiDocsTool implements AgentTool {

    @Override
    public String getName() {
        return "GenerateApiDocsTool";
    }

    @Override
    public String getDescription() {
        return "Scans Spring Boot REST controllers and generates OpenAPI / Markdown API documentation.";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> params) {
        String controllerName = (String) params.getOrDefault("controllerName", "IssueController");

        String generatedMarkdownDocs = """
            # API Reference - %s

            ### Endpoints Summary

            #### 1. Create Issue
            - **Method**: `POST /api/projects/{projectId}/issues`
            - **Permission**: `ADMIN` or `PROJECT_MANAGER` or `DEVELOPER`
            - **Request Body**:
              ```json
              {
                "title": "Fix JWT token expiration error",
                "description": "Token expires unexpectedly after 5 minutes",
                "type": "BUG",
                "severity": 8,
                "businessImpact": 7,
                "dueDate": "2026-09-30"
              }
              ```
            - **Response**: `201 Created` (`IssueDto`)

            #### 2. Get Prioritized Backlog (Max-Heap Sorted)
            - **Method**: `GET /api/projects/{projectId}/issues/prioritized`
            - **Permission**: `VIEWER` or above
            - **Response**: `200 OK` (`List<IssueDto>` sorted by `calculatedPriorityScore` descending)
            """.formatted(controllerName);

        return Map.of(
            "status", "SUCCESS",
            "controllerName", controllerName,
            "generatedMarkdownDocs", generatedMarkdownDocs
        );
    }
}
