package com.devopsai.backend.service;

import com.devopsai.backend.dto.ConnectGithubRepoRequest;
import com.devopsai.backend.dto.GithubRepoDto;
import com.devopsai.backend.entity.GithubRepository;
import com.devopsai.backend.entity.Project;
import com.devopsai.backend.entity.Role;
import com.devopsai.backend.entity.SyncStatus;
import com.devopsai.backend.entity.User;
import com.devopsai.backend.exception.ResourceNotFoundException;
import com.devopsai.backend.repository.ChangedFileRepository;
import com.devopsai.backend.repository.CommitRepository;
import com.devopsai.backend.repository.GithubRepositoryRepository;
import com.devopsai.backend.repository.ProjectRepository;
import com.devopsai.backend.repository.PullRequestRepository;
import com.devopsai.backend.security.TokenEncryptionConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GithubIntegrationServiceTest {

    @Mock
    private GithubRepositoryRepository githubRepositoryRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private CommitRepository commitRepository;

    @Mock
    private PullRequestRepository pullRequestRepository;

    @Mock
    private ChangedFileRepository changedFileRepository;

    @Mock
    private GithubApiClient githubApiClient;

    @Spy
    private TokenEncryptionConverter tokenEncryptionConverter = new TokenEncryptionConverter();

    @InjectMocks
    private GithubIntegrationService githubIntegrationService;

    private Project testProject;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("admin", "admin@devopsai.io", "password", Role.ADMIN);
        testUser.setId(1L);

        testProject = new Project("SmartHealth", "Telemetry Platform", "SMART", testUser);
        testProject.setId(10L);
    }

    @Test
    @DisplayName("Should successfully connect repository and return masked access token in DTO")
    void testConnectGithubRepoSuccess() {
        ConnectGithubRepoRequest request = new ConnectGithubRepoRequest();
        request.setOwnerName("devopsai");
        request.setRepoName("smarthealth-platform");
        request.setDefaultBranch("main");
        request.setAccessToken("ghp_1234567890abcdefghijklmnopqrstuvwxyz");

        when(projectRepository.findById(10L)).thenReturn(Optional.of(testProject));
        when(githubRepositoryRepository.findByProjectId(10L)).thenReturn(Optional.empty());
        when(githubRepositoryRepository.save(any(GithubRepository.class))).thenAnswer(invocation -> {
            GithubRepository repo = invocation.getArgument(0);
            repo.setId(100L);
            return repo;
        });

        GithubRepoDto dto = githubIntegrationService.connectRepository(10L, request);

        assertNotNull(dto);
        assertEquals("devopsai", dto.getOwnerName());
        assertEquals("smarthealth-platform", dto.getRepoName());
        assertTrue(dto.getMaskedAccessToken().startsWith("ghp_"));
        assertTrue(dto.getMaskedAccessToken().endsWith("wxyz"));
        verify(githubRepositoryRepository, times(1)).save(any(GithubRepository.class));
    }

    @Test
    @DisplayName("Should trigger async sync and verify background worker invocation")
    void testTriggerAsyncSyncSuccess() {
        GithubRepository repo = new GithubRepository("devopsai", "smarthealth-platform", "main", "ghp_secretToken123456789", testProject);
        repo.setId(100L);
        repo.setSyncStatus(SyncStatus.PENDING);

        when(githubRepositoryRepository.findById(100L)).thenReturn(Optional.of(repo));

        githubIntegrationService.syncRepositoryAsync(100L);

        verify(githubRepositoryRepository, atLeastOnce()).save(repo);
    }

    @Test
    @DisplayName("Should encrypt access token at rest and decrypt back using TokenEncryptionConverter")
    void testTokenEncryptionConverter() {
        String rawToken = "ghp_SecretAccessTokenForGithubIntegration123";
        
        String encrypted = tokenEncryptionConverter.convertToDatabaseColumn(rawToken);
        assertNotNull(encrypted);
        assertNotEquals(rawToken, encrypted);

        String decrypted = tokenEncryptionConverter.convertToEntityAttribute(encrypted);
        assertEquals(rawToken, decrypted);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when connecting repo for non-existent project")
    void testConnectGithubRepoProjectNotFound() {
        ConnectGithubRepoRequest request = new ConnectGithubRepoRequest();
        request.setOwnerName("devopsai");
        request.setRepoName("test");
        request.setAccessToken("ghp_123");

        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> githubIntegrationService.connectRepository(99L, request));
    }
}
