package com.devopsai.backend.service;

import com.devopsai.backend.dto.IssueDto;
import com.devopsai.backend.dto.IssueRequest;
import com.devopsai.backend.entity.*;
import com.devopsai.backend.exception.ResourceNotFoundException;
import com.devopsai.backend.repository.*;
import com.devopsai.backend.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EpicRepository epicRepository;

    @Mock
    private SprintRepository sprintRepository;

    @org.mockito.Spy
    private PriorityCalculator priorityCalculator = new PriorityCalculator();

    private org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication;

    @InjectMocks
    private IssueService issueService;

    private Project testProject;
    private User testUser;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        testUser = new User("dev_user", "dev@devopsai.io", "password", Role.DEVELOPER);
        testUser.setId(10L);

        userPrincipal = UserPrincipal.create(testUser);

        testProject = new Project("SmartHealth", "Telemetry Platform", "SMART", testUser);
        testProject.setId(1L);

        authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
    }

    @Test
    @DisplayName("Should successfully create issue and calculate priority score")
    void testCreateIssueSuccess() {
        IssueRequest request = new IssueRequest();
        request.setTitle("Fix Database Connection Leak");
        request.setDescription("HikariCP pool exhausts under load");
        request.setType(IssueType.BUG);
        request.setStatus(IssueStatus.TODO);
        request.setPriority(IssuePriority.HIGH);
        request.setSeverity(8);
        request.setBusinessImpact(9);
        request.setDueDate(LocalDate.now().plusDays(3));
        request.setDependenciesCount(1);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(10L)).thenReturn(Optional.of(testUser));
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(100L);
            return issue;
        });

        IssueDto created = issueService.createIssue(1L, request, authentication);

        assertNotNull(created);
        assertEquals(100L, created.getId());
        assertEquals("Fix Database Connection Leak", created.getTitle());
        assertEquals(IssueType.BUG, created.getType());
        verify(priorityCalculator, times(1)).calculateScore(any(Issue.class));
        verify(issueRepository, times(1)).save(any(Issue.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when project not found")
    void testCreateIssueProjectNotFound() {
        IssueRequest request = new IssueRequest();
        request.setTitle("Test Issue");

        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> issueService.createIssue(99L, request, authentication));
    }

    @Test
    @DisplayName("Should update issue status successfully")
    void testUpdateIssueStatus() {
        Issue existingIssue = new Issue("Task Title", "Description", IssueType.TASK, IssueStatus.TODO,
            IssuePriority.MEDIUM, 5, 5, LocalDate.now(), 0, testProject, testUser);
        existingIssue.setId(50L);

        when(issueRepository.findById(50L)).thenReturn(Optional.of(existingIssue));
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IssueDto updated = issueService.updateIssueStatus(50L, IssueStatus.CODE_REVIEW);

        assertNotNull(updated);
        assertEquals(IssueStatus.CODE_REVIEW, updated.getStatus());
        verify(issueRepository, times(1)).save(existingIssue);
    }

    @Test
    @DisplayName("Should fetch prioritized backlog using PriorityCalculator ranking")
    void testGetPrioritizedBacklog() {
        Issue i1 = new Issue();
        i1.setId(1L);
        Issue i2 = new Issue();
        i2.setId(2L);

        List<Issue> rawIssues = Arrays.asList(i1, i2);
        List<Issue> rankedIssues = Arrays.asList(i2, i1);

        when(projectRepository.existsById(1L)).thenReturn(true);
        when(issueRepository.findByProjectId(1L)).thenReturn(rawIssues);
        when(priorityCalculator.rankIssues(rawIssues)).thenReturn(rankedIssues);

        List<IssueDto> backlog = issueService.getPrioritizedBacklog(1L);

        assertEquals(2, backlog.size());
        assertEquals(2L, backlog.get(0).getId());
        assertEquals(1L, backlog.get(1).getId());
        verify(issueRepository, times(1)).saveAll(rankedIssues);
    }
}
