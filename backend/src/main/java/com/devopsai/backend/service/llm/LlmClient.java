package com.devopsai.backend.service.llm;

import com.devopsai.backend.dto.CodeReviewFindingDto;
import java.util.List;

public interface LlmClient {
    /**
     * Analyzes source code file content and produces a structured list of findings.
     */
    List<CodeReviewFindingDto> analyzeCode(String filename, String codeContent);

    /**
     * Generates text completion for general LLM prompts.
     */
    String generateCompletion(String prompt);
}
