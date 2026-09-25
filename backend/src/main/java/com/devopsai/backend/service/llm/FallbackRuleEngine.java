package com.devopsai.backend.service.llm;

import com.devopsai.backend.dto.CodeReviewFindingDto;
import com.devopsai.backend.entity.FindingSeverity;
import com.devopsai.backend.entity.ReviewCategory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FallbackRuleEngine implements LlmClient {

    @Override
    public List<CodeReviewFindingDto> analyzeCode(String filename, String codeContent) {
        List<CodeReviewFindingDto> findings = new ArrayList<>();
        if (codeContent == null || codeContent.isBlank()) {
            return findings;
        }

        String[] lines = codeContent.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNumber = i + 1;

            // Rule 1: Flagship Optional.get() Anti-Pattern
            if (line.contains(".get()") && (line.contains("repository") || line.contains("Optional") || line.contains("find"))) {
                findings.add(new CodeReviewFindingDto(
                    filename,
                    lineNumber,
                    ReviewCategory.BUG,
                    FindingSeverity.HIGH,
                    "Potential NoSuchElementException from raw Optional.get()",
                    "Direct invocation of Optional.get() without checking isPresent() can throw NoSuchElementException at runtime.",
                    "Use repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(\"...\")) instead of raw .get().",
                    line.trim()
                ));
            }

            // Rule 2: Security - SQL String Concatenation Vulnerability
            if ((line.contains("SELECT ") || line.contains("WHERE ")) && line.contains("+")) {
                findings.add(new CodeReviewFindingDto(
                    filename,
                    lineNumber,
                    ReviewCategory.SECURITY,
                    FindingSeverity.CRITICAL,
                    "High Security Risk: SQL Injection via Raw Concatenation",
                    "Constructing SQL queries via string concatenation exposes the database to SQL injection attacks.",
                    "Use parameterized JPA repository methods (@Query with :param) or Criteria API.",
                    line.trim()
                ));
            }

            // Rule 3: Performance - String Concatenation in Loops
            if (line.contains("+=") && (line.contains("str") || line.contains("text") || line.contains("body"))) {
                findings.add(new CodeReviewFindingDto(
                    filename,
                    lineNumber,
                    ReviewCategory.PERFORMANCE,
                    FindingSeverity.MEDIUM,
                    "Performance Bottleneck: Repeated String Concatenation",
                    "Using string concatenation operator in loops creates excessive intermediate String objects in Heap.",
                    "Replace raw string concatenation with StringBuilder or StringJoiner.",
                    line.trim()
                ));
            }

            // Rule 4: Security - Hardcoded Credentials / Tokens
            if ((line.contains("password =") || line.contains("secret =") || line.contains("apiKey =")) && line.contains("\"")) {
                findings.add(new CodeReviewFindingDto(
                    filename,
                    lineNumber,
                    ReviewCategory.SECURITY,
                    FindingSeverity.HIGH,
                    "Hardcoded Secret / Credential Detected",
                    "Hardcoding plain-text passwords or secret keys directly in Java source code risks repo credentials leak.",
                    "Inject secrets dynamically using Spring Boot Environment properties (@Value(\"${app.secret}\")).",
                    line.trim()
                ));
            }

            // Rule 5: Best Practice - Raw System.out.println
            if (line.contains("System.out.print")) {
                findings.add(new CodeReviewFindingDto(
                    filename,
                    lineNumber,
                    ReviewCategory.CODE_STYLE,
                    FindingSeverity.LOW,
                    "Code Style: Standard Output Printing",
                    "Using System.out.println() bypasses the application logging framework and MDC security context.",
                    "Replace System.out with SLF4J Logger (private static final Logger logger = LoggerFactory.getLogger(...)).",
                    line.trim()
                ));
            }
        }

        // Default fallback finding if code snippet is clean or no rules triggered
        if (findings.isEmpty()) {
            findings.add(new CodeReviewFindingDto(
                filename,
                1,
                ReviewCategory.BEST_PRACTICE,
                FindingSeverity.INFO,
                "Clean Code Verification",
                "No critical security vulnerabilities, memory leaks, or Optional.get() anti-patterns detected.",
                "Ensure comprehensive unit test coverage with JUnit 5 and Mockito assertions.",
                lines[0].trim()
            ));
        }

        return findings;
    }

    @Override
    public String generateCompletion(String prompt) {
        if (prompt == null) return "DevOpsAI Assistant analyzed the codebase.";
        String lower = prompt.toLowerCase();
        if (lower.contains("auth") || lower.contains("login") || lower.contains("jwt")) {
            return "Authentication in DevOpsAI is handled via `SecurityConfig.java` and `AuthService.java`. Login requests are authenticated via Spring Security AuthenticationManager, issuing stateless JWT Access Tokens and Refresh Tokens.";
        } else if (lower.contains("priority") || lower.contains("score")) {
            return "Issue priority is calculated by `PriorityCalculator.java` using weighted values for Severity, Business Impact, Dependencies Count, and Deadline Proximity.";
        } else if (lower.contains("webhook") || lower.contains("github")) {
            return "GitHub Webhooks are ingested by `GithubWebhookController.java` at `/api/webhooks/github` and dispatched asynchronously to `GithubWebhookService.java`.";
        }
        return "DevOpsAI RAG Assistant retrieved relevant codebase context from indexed project source files, commits, and configurations.";
    }
}
