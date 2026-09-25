package com.devopsai.backend.service;

import com.devopsai.backend.repository.ChangedFileRepository;
import com.devopsai.backend.repository.CommitRepository;
import com.devopsai.backend.repository.GithubRepositoryRepository;
import com.devopsai.backend.repository.PullRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GithubWebhookServiceTest {

    @Mock
    private GithubRepositoryRepository githubRepositoryRepository;

    @Mock
    private CommitRepository commitRepository;

    @Mock
    private PullRequestRepository pullRequestRepository;

    @Mock
    private ChangedFileRepository changedFileRepository;

    @Mock
    private GithubApiClient githubApiClient;

    @InjectMocks
    private GithubWebhookService githubWebhookService;

    private String webhookSecret;
    private String payload;
    private String validSignature;

    @BeforeEach
    void setUp() throws Exception {
        webhookSecret = "secret_webhook_key_123";
        payload = "{\"action\":\"closed\",\"pull_request\":{\"number\":42,\"state\":\"closed\",\"merged\":true}}";
        validSignature = calculateHmacSha256(payload, webhookSecret);
    }

    private String calculateHmacSha256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder("sha256=");
        for (byte b : hmacBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    @Test
    @DisplayName("Should return true for valid HMAC SHA-256 webhook signature")
    void testVerifySignatureSuccess() {
        boolean isValid = githubWebhookService.verifySignature(payload, validSignature, webhookSecret);
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should return false for invalid signature or mismatched secret")
    void testVerifySignatureInvalid() {
        boolean isValid = githubWebhookService.verifySignature(payload, "sha256=invalid1234567890", webhookSecret);
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should handle missing secret or invalid signature header appropriately")
    void testVerifySignatureNulls() {
        assertFalse(githubWebhookService.verifySignature(payload, null, webhookSecret));
        assertTrue(githubWebhookService.verifySignature(payload, validSignature, null));
    }
}
