package com.devopsai.backend.service;

import com.devopsai.backend.entity.*;
import com.devopsai.backend.repository.CommitRepository;
import com.devopsai.backend.repository.GithubRepositoryRepository;
import com.devopsai.backend.repository.PullRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Map;

@Service
public class GithubWebhookService {

    private static final Logger logger = LoggerFactory.getLogger(GithubWebhookService.class);

    private final GithubRepositoryRepository githubRepositoryRepository;
    private final CommitRepository commitRepository;
    private final PullRequestRepository pullRequestRepository;
    private final SecurityScannerService securityScannerService;

    public GithubWebhookService(GithubRepositoryRepository githubRepositoryRepository,
                                 CommitRepository commitRepository,
                                 PullRequestRepository pullRequestRepository,
                                 SecurityScannerService securityScannerService) {
        this.githubRepositoryRepository = githubRepositoryRepository;
        this.commitRepository = commitRepository;
        this.pullRequestRepository = pullRequestRepository;
        this.securityScannerService = securityScannerService;
    }

    /**
     * Verifies the HMAC SHA-256 signature from GitHub webhook header (X-Hub-Signature-256).
     */
    public boolean verifySignature(String payload, String signatureHeader, String secret) {
        if (secret == null || secret.isBlank() || signatureHeader == null || !signatureHeader.startsWith("sha256=")) {
            // If secret is not set or header is missing, return true for flexible local testing
            return secret == null || secret.isBlank();
        }

        try {
            String expectedSignature = signatureHeader.substring(7);
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hmacBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = HexFormat.of().formatHex(hmacBytes);

            return MessageDigestEquals(expectedSignature, calculatedSignature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("HMAC SHA-256 calculation error during webhook verification", e);
            return false;
        }
    }

    private boolean MessageDigestEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    /**
     * Asynchronously processes incoming webhook payloads (push and pull_request events).
     */
    @Async("githubTaskExecutor")
    @Transactional
    public void processWebhookEventAsync(String eventType, Map<String, Object> payload) {
        logger.info("Processing webhook event asynchronously: {}", eventType);

        if ("push".equalsIgnoreCase(eventType)) {
            handlePushEvent(payload);
        } else if ("pull_request".equalsIgnoreCase(eventType)) {
            handlePullRequestEvent(payload);
        } else {
            logger.info("Ignored webhook event type: {}", eventType);
        }
    }

    @SuppressWarnings("unchecked")
    private void handlePushEvent(Map<String, Object> payload) {
        Map<String, Object> repository = (Map<String, Object>) payload.get("repository");
        if (repository == null) return;

        String repoName = (String) repository.get("name");
        Map<String, Object> owner = (Map<String, Object>) repository.get("owner");
        String ownerName = owner != null ? (String) owner.get("name") : null;
        if (ownerName == null && owner != null) {
            ownerName = (String) owner.get("login");
        }

        if (ownerName != null && repoName != null) {
            githubRepositoryRepository.findByOwnerNameAndRepoName(ownerName, repoName).ifPresent(repo -> {
                String headCommitSha = (String) payload.get("after");
                if (headCommitSha != null && !headCommitSha.isBlank()) {
                    String pusherName = "developer";
                    if (payload.get("pusher") instanceof Map pusherMap) {
                        pusherName = (String) pusherMap.getOrDefault("name", "developer");
                    }
                    Commit commit = new Commit(
                        headCommitSha,
                        "Push event update via webhook",
                        pusherName,
                        "dev@devopsai.io",
                        LocalDateTime.now(),
                        repo.getHtmlUrl(),
                        repo
                    );
                    commitRepository.save(commit);
                    logger.info("Saved new push commit from webhook: {}", headCommitSha);
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private void handlePullRequestEvent(Map<String, Object> payload) {
        Map<String, Object> prMap = (Map<String, Object>) payload.get("pull_request");
        Map<String, Object> repository = (Map<String, Object>) payload.get("repository");
        if (prMap == null || repository == null) return;

        String repoName = (String) repository.get("name");
        Map<String, Object> owner = (Map<String, Object>) repository.get("owner");
        String ownerName = owner != null ? (String) owner.get("login") : null;

        if (ownerName != null && repoName != null) {
            githubRepositoryRepository.findByOwnerNameAndRepoName(ownerName, repoName).ifPresent(repo -> {
                int prNumber = (Integer) prMap.get("number");
                String title = (String) prMap.get("title");
                String body = (String) prMap.get("body");
                String stateStr = (String) prMap.get("state");

                PullRequestState state = "closed".equalsIgnoreCase(stateStr) ? PullRequestState.CLOSED : PullRequestState.OPEN;
                if (prMap.get("merged_at") != null) {
                    state = PullRequestState.MERGED;
                }

                PullRequest pr = pullRequestRepository.findByGithubRepositoryIdAndNumber(repo.getId(), prNumber)
                    .orElseGet(() -> new PullRequest(prNumber, title, body, PullRequestState.OPEN, "feature", "main", "dev_user", 1, 10, 2, repo));

                pr.setTitle(title);
                pr.setDescription(body);
                pr.setState(state);

                pullRequestRepository.save(pr);
                logger.info("Saved PR #{} from webhook event: state={}", prNumber, state);

                // Asynchronously trigger automated security scan for PR commit
                try {
                    securityScannerService.runSecurityScan(repo.getProject().getId(),
                            new com.devopsai.backend.dto.TriggerSecurityScanRequest(repo.getProject().getId(), "pr-" + prNumber, false));
                    logger.info("Triggered automated background security scan for PR #{}", prNumber);
                } catch (Exception e) {
                    logger.warn("Automated security scan failed for PR #{}: {}", prNumber, e.getMessage());
                }
            });
        }
    }
}
