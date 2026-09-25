package com.devopsai.backend.service;

import com.devopsai.backend.dto.CodeReviewFindingDto;
import com.devopsai.backend.entity.FindingSeverity;
import com.devopsai.backend.entity.ReviewCategory;
import com.devopsai.backend.service.llm.FallbackRuleEngine;
import com.devopsai.backend.service.llm.StructuredLlmClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LlmClientTest {

    private FallbackRuleEngine fallbackRuleEngine;
    private StructuredLlmClient structuredLlmClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        fallbackRuleEngine = new FallbackRuleEngine();
        objectMapper = new ObjectMapper();
        structuredLlmClient = new StructuredLlmClient(fallbackRuleEngine, objectMapper);
    }

    @Test
    @DisplayName("Should detect flagship Optional.get() anti-pattern in FallbackRuleEngine")
    void testFallbackRuleEngineOptionalGetDetection() {
        String codeSnippet = """
            public User getPatient(Long id) {
                return repository.findById(id).get();
            }
            """;

        List<CodeReviewFindingDto> findings = fallbackRuleEngine.analyzeCode("TelemetryService.java", codeSnippet);

        assertNotNull(findings);
        assertFalse(findings.isEmpty());
        
        CodeReviewFindingDto finding = findings.stream()
            .filter(f -> f.getCategory() == ReviewCategory.BUG)
            .findFirst()
            .orElse(null);

        assertNotNull(finding);
        assertEquals(ReviewCategory.BUG, finding.getCategory());
        assertEquals(FindingSeverity.HIGH, finding.getSeverity());
        assertTrue(finding.getTitle().contains("Optional.get()"));
        assertTrue(finding.getRecommendation().contains("orElseThrow"));
    }

    @Test
    @DisplayName("Should detect SQL Injection raw string concatenation in FallbackRuleEngine")
    void testFallbackRuleEngineSqlInjectionDetection() {
        String codeSnippet = "String query = \"SELECT * FROM users WHERE name = '\" + username + \"'\";";

        List<CodeReviewFindingDto> findings = fallbackRuleEngine.analyzeCode("QueryBuilder.java", codeSnippet);

        assertNotNull(findings);
        CodeReviewFindingDto finding = findings.stream()
            .filter(f -> f.getCategory() == ReviewCategory.SECURITY)
            .findFirst()
            .orElse(null);

        assertNotNull(finding);
        assertEquals(FindingSeverity.CRITICAL, finding.getSeverity());
        assertTrue(finding.getTitle().contains("SQL Injection"));
    }

    @Test
    @DisplayName("Should parse structured JSON array response from LLM API")
    void testParseFindingsJsonSuccess() {
        String jsonPayload = """
            [
              {
                "filename": "AuthController.java",
                "lineNumber": 42,
                "category": "SECURITY",
                "severity": "HIGH",
                "title": "Insecure Secret",
                "description": "Hardcoded JWT secret",
                "recommendation": "Use @Value annotation",
                "codeSnippet": "String secret = \\"abc\\";"
              }
            ]
            """;

        List<CodeReviewFindingDto> dtos = structuredLlmClient.parseFindingsJson(jsonPayload, "AuthController.java");

        assertNotNull(dtos);
        assertEquals(1, dtos.size());
        CodeReviewFindingDto dto = dtos.get(0);
        assertEquals("AuthController.java", dto.getFilename());
        assertEquals(42, dto.getLineNumber());
        assertEquals(ReviewCategory.SECURITY, dto.getCategory());
        assertEquals(FindingSeverity.HIGH, dto.getSeverity());
    }
}
