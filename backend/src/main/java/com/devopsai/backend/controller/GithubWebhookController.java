package com.devopsai.backend.controller;

import com.devopsai.backend.service.GithubWebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
public class GithubWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(GithubWebhookController.class);
    private final GithubWebhookService githubWebhookService;

    public GithubWebhookController(GithubWebhookService githubWebhookService) {
        this.githubWebhookService = githubWebhookService;
    }

    @PostMapping("/github")
    public ResponseEntity<Map<String, String>> handleGithubWebhook(
        @RequestHeader(name = "X-GitHub-Event", defaultValue = "push") String eventType,
        @RequestHeader(name = "X-Hub-Signature-256", required = false) String signatureHeader,
        @RequestBody Map<String, Object> payload
    ) {
        logger.info("Received GitHub Webhook Event: {}", eventType);

        // Process webhook event asynchronously in background thread pool
        githubWebhookService.processWebhookEventAsync(eventType, payload);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(Map.of("status", "ACCEPTED", "message", "Webhook event queued for processing"));
    }
}
