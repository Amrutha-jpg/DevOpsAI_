package com.devopsai.backend.service.agent.tools;

import com.devopsai.backend.dto.CodeReviewFindingDto;
import com.devopsai.backend.service.llm.FallbackRuleEngine;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AnalyzeVulnerabilitiesTool implements AgentTool {

    private final FallbackRuleEngine fallbackRuleEngine;

    public AnalyzeVulnerabilitiesTool(FallbackRuleEngine fallbackRuleEngine) {
        this.fallbackRuleEngine = fallbackRuleEngine;
    }

    @Override
    public String getName() {
        return "AnalyzeVulnerabilitiesTool";
    }

    @Override
    public String getDescription() {
        return "Scans source code snippet for security vulnerabilities, SQL injection risks, and Optional.get() anti-patterns.";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> params) {
        String filename = (String) params.getOrDefault("filename", "SourceFile.java");
        String codeContent = (String) params.getOrDefault("codeContent", "public User getUser(Long id) { return repository.findById(id).get(); }");

        List<CodeReviewFindingDto> findings = fallbackRuleEngine.analyzeCode(filename, codeContent);

        return Map.of(
            "status", "SUCCESS",
            "filename", filename,
            "vulnerabilitiesFound", findings.size(),
            "findings", findings
        );
    }
}
