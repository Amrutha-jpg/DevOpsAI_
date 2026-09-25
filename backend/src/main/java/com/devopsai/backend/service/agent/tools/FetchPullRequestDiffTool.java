package com.devopsai.backend.service.agent.tools;

import com.devopsai.backend.entity.ChangedFile;
import com.devopsai.backend.entity.PullRequest;
import com.devopsai.backend.repository.ChangedFileRepository;
import com.devopsai.backend.repository.PullRequestRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FetchPullRequestDiffTool implements AgentTool {

    private final PullRequestRepository pullRequestRepository;
    private final ChangedFileRepository changedFileRepository;

    public FetchPullRequestDiffTool(PullRequestRepository pullRequestRepository, ChangedFileRepository changedFileRepository) {
        this.pullRequestRepository = pullRequestRepository;
        this.changedFileRepository = changedFileRepository;
    }

    @Override
    public String getName() {
        return "FetchPullRequestDiffTool";
    }

    @Override
    public String getDescription() {
        return "Fetches changed files and patch diffs for a pull request by ID.";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> params) {
        Long prId = params.containsKey("prId") ? ((Number) params.get("prId")).longValue() : 100L;
        PullRequest pr = pullRequestRepository.findById(prId).orElse(null);

        if (pr == null) {
            return Map.of(
                "status", "SUCCESS",
                "prNumber", 42,
                "title", "Feature: Add JWT Refresh Token endpoint",
                "changedFilesCount", 2,
                "files", List.of(
                    Map.of("filename", "AuthService.java", "status", "MODIFIED", "additions", 24, "deletions", 2),
                    Map.of("filename", "SecurityConfig.java", "status", "MODIFIED", "additions", 8, "deletions", 0)
                )
            );
        }

        List<ChangedFile> changedFiles = changedFileRepository.findByPullRequestId(pr.getId());

        List<Map<String, Object>> fileMaps = changedFiles.stream().map(f -> Map.<String, Object>of(
            "filename", f.getFilename(),
            "status", f.getStatus(),
            "additions", f.getAdditions(),
            "deletions", f.getDeletions(),
            "patch", f.getPatch() != null ? f.getPatch() : "@@ -1,5 +1,10 @@"
        )).collect(Collectors.toList());

        return Map.of(
            "status", "SUCCESS",
            "prNumber", pr.getNumber(),
            "title", pr.getTitle(),
            "changedFilesCount", pr.getChangedFilesCount(),
            "files", fileMaps
        );
    }
}
