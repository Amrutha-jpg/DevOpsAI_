package com.devopsai.backend.config;

import com.devopsai.backend.entity.*;
import com.devopsai.backend.repository.*;
import com.devopsai.backend.service.PriorityCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final EpicRepository epicRepository;
    private final SprintRepository sprintRepository;
    private final IssueRepository issueRepository;
    private final PriorityCalculator priorityCalculator;
    private final PasswordEncoder passwordEncoder;
    private final GithubRepositoryRepository githubRepositoryRepository;
    private final CommitRepository commitRepository;
    private final PullRequestRepository pullRequestRepository;
    private final ChangedFileRepository changedFileRepository;
    private final CodeReviewRepository codeReviewRepository;

    public DataInitializer(UserRepository userRepository,
                           ProjectRepository projectRepository,
                           ProjectMemberRepository projectMemberRepository,
                           EpicRepository epicRepository,
                           SprintRepository sprintRepository,
                           IssueRepository issueRepository,
                           PriorityCalculator priorityCalculator,
                           PasswordEncoder passwordEncoder,
                           GithubRepositoryRepository githubRepositoryRepository,
                           CommitRepository commitRepository,
                           PullRequestRepository pullRequestRepository,
                           ChangedFileRepository changedFileRepository,
                           CodeReviewRepository codeReviewRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.epicRepository = epicRepository;
        this.sprintRepository = sprintRepository;
        this.issueRepository = issueRepository;
        this.priorityCalculator = priorityCalculator;
        this.passwordEncoder = passwordEncoder;
        this.githubRepositoryRepository = githubRepositoryRepository;
        this.commitRepository = commitRepository;
        this.pullRequestRepository = pullRequestRepository;
        this.changedFileRepository = changedFileRepository;
        this.codeReviewRepository = codeReviewRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            logger.info("Data already seeded. Skipping initial data setup.");
            return;
        }

        logger.info("Seeding initial DevOpsAI demo users, projects, epics, sprints, issues, and GitHub integration...");

        String defaultPassword = passwordEncoder.encode("password123");

        User admin = userRepository.save(new User("admin", "admin@devopsai.io", defaultPassword, Role.ADMIN));
        User pmUser = userRepository.save(new User("pm_user", "pm@devopsai.io", defaultPassword, Role.PROJECT_MANAGER));
        User devUser = userRepository.save(new User("dev_user", "dev@devopsai.io", defaultPassword, Role.DEVELOPER));
        User viewerUser = userRepository.save(new User("viewer_user", "viewer@devopsai.io", defaultPassword, Role.VIEWER));

        Project p1 = projectRepository.save(new Project(
            "SmartHealth Platform",
            "AI-powered healthcare diagnostic and telemetry platform",
            "SMART",
            pmUser
        ));

        Project p2 = projectRepository.save(new Project(
            "CloudOps Automator",
            "Infrastructure as Code and automated CI/CD monitoring system",
            "CLOUDOPS",
            admin
        ));

        projectMemberRepository.save(new ProjectMember(p1, pmUser, ProjectRole.PROJECT_MANAGER));
        projectMemberRepository.save(new ProjectMember(p1, devUser, ProjectRole.DEVELOPER));
        projectMemberRepository.save(new ProjectMember(p1, viewerUser, ProjectRole.VIEWER));

        projectMemberRepository.save(new ProjectMember(p2, admin, ProjectRole.PROJECT_MANAGER));
        projectMemberRepository.save(new ProjectMember(p2, devUser, ProjectRole.DEVELOPER));

        // Seed Epics for SmartHealth
        Epic epicCore = epicRepository.save(new Epic("Core Authentication & Telemetry", "Patient telemetry data stream and auth system", p1));
        Epic epicAI = epicRepository.save(new Epic("AI Diagnostics & Analytics", "ML model scoring and diagnostic recommendations", p1));

        // Seed Sprints for SmartHealth
        Sprint sprint1 = sprintRepository.save(new Sprint("Sprint 1 - Foundation", "Build Core Telemetry API", LocalDate.now().minusDays(5), LocalDate.now().plusDays(9), p1));
        sprint1.setStatus(SprintStatus.ACTIVE);
        sprintRepository.save(sprint1);

        Sprint sprint2 = sprintRepository.save(new Sprint("Sprint 2 - AI Integration", "Deploy ML scoring pipeline", LocalDate.now().plusDays(10), LocalDate.now().plusDays(24), p1));

        // Seed Issues for SmartHealth (Bugs & Tasks with varying Severity, Impact, Due Dates)
        Issue issue1 = new Issue(
            "Critical: Fix NullPointer in Patient Telemetry Stream",
            "Telemetry stream crashes when sensor data payload contains null readings.",
            IssueType.BUG,
            IssueStatus.IN_PROGRESS,
            IssuePriority.CRITICAL,
            10, // severity
            9,  // business impact
            LocalDate.now().minusDays(1), // Overdue -> clamped to 1.0 divisor
            2,  // dependencies
            p1,
            devUser
        );
        issue1.setEpic(epicCore);
        issue1.setSprint(sprint1);
        issue1.setAssignee(devUser);
        priorityCalculator.calculateScore(issue1);
        issueRepository.save(issue1);

        Issue issue2 = new Issue(
            "Implement OAuth2 JWT Auth Handler",
            "Secure REST API endpoints with signed JWT access and refresh tokens.",
            IssueType.TASK,
            IssueStatus.DONE,
            IssuePriority.HIGH,
            8,
            8,
            LocalDate.now().plusDays(5),
            1,
            p1,
            pmUser
        );
        issue2.setEpic(epicCore);
        issue2.setSprint(sprint1);
        issue2.setAssignee(devUser);
        priorityCalculator.calculateScore(issue2);
        issueRepository.save(issue2);

        Issue issue3 = new Issue(
            "High Security Vulnerability: SQL Injection Risk in Diagnostic Query",
            "Raw string concat in diagnostic search repository query exposed to injection.",
            IssueType.BUG,
            IssueStatus.CODE_REVIEW,
            IssuePriority.CRITICAL,
            9,
            10,
            LocalDate.now().plusDays(2),
            3,
            p1,
            devUser
        );
        issue3.setEpic(epicAI);
        issue3.setSprint(sprint1);
        issue3.setAssignee(devUser);
        priorityCalculator.calculateScore(issue3);
        issueRepository.save(issue3);

        Issue issue4 = new Issue(
            "Design Patient Analytics Dashboard UI",
            "Create React components for patient diagnostic telemetry charts.",
            IssueType.TASK,
            IssueStatus.TODO,
            IssuePriority.MEDIUM,
            4,
            5,
            LocalDate.now().plusDays(12),
            0,
            p1,
            pmUser
        );
        issue4.setEpic(epicAI);
        issue4.setSprint(sprint2);
        issue4.setAssignee(viewerUser);
        priorityCalculator.calculateScore(issue4);
        issueRepository.save(issue4);

        // Seed GitHub Integration Data for SmartHealth Platform (p1)
        GithubRepository ghRepo = new GithubRepository(
            "devopsai",
            "smarthealth-platform",
            "main",
            "ghp_demoSecretToken9876543210abcdefghijkl",
            p1
        );
        ghRepo.setGithubRepoId(987654321L);
        ghRepo.setSyncStatus(SyncStatus.COMPLETED);
        ghRepo.setLastSyncedAt(java.time.LocalDateTime.now());
        ghRepo.setWebhookSecret("webhook_secret_devopsai_demo_123");
        ghRepo = githubRepositoryRepository.save(ghRepo);

        // Seed Commits
        commitRepository.save(new Commit(
            "a1b2c3d4e5f67890123456789abcdef012345678",
            "feat(telemetry): Add null check and fallback logic for sensor streams",
            "dev_user",
            "dev@devopsai.io",
            java.time.LocalDateTime.now().minusHours(4),
            "https://github.com/devopsai/smarthealth-platform/commit/a1b2c3d4e5f67890123456789abcdef012345678",
            ghRepo
        ));

        commitRepository.save(new Commit(
            "f9e8d7c6b5a43210987654321fedcba098765432",
            "sec(auth): Implement JWT token validation filter and security context",
            "pm_user",
            "pm@devopsai.io",
            java.time.LocalDateTime.now().minusHours(2),
            "https://github.com/devopsai/smarthealth-platform/commit/f9e8d7c6b5a43210987654321fedcba098765432",
            ghRepo
        ));

        // Seed Pull Request #42
        PullRequest pr42 = pullRequestRepository.save(new PullRequest(
            42,
            "Add telemetry stream & auth pipeline",
            "This pull request addresses issue SMART-1 by resolving sensor null checks and implementing JWT authorization headers for diagnostic endpoints.",
            PullRequestState.OPEN,
            "feature/telemetry-auth",
            "main",
            "dev_user",
            2, // changed files
            45, // additions
            12, // deletions
            ghRepo
        ));

        // Seed Changed Files for PR #42
        changedFileRepository.save(new ChangedFile(
            "src/main/java/com/devopsai/smarthealth/service/TelemetryService.java",
            "modified",
            28,
            5,
            "@@ -15,7 +15,30 @@ public class TelemetryService {\n-    if (data == null) throw new RuntimeException();\n+    if (data == null || data.getReadings() == null) {\n+        logger.warn(\"Received empty telemetry payload, applying fallback defaults\");\n+        return TelemetryPayload.empty();\n+    }\n+    return processReadings(data);",
            pr42
        ));

        changedFileRepository.save(new ChangedFile(
            "src/main/java/com/devopsai/smarthealth/controller/AuthController.java",
            "modified",
            17,
            7,
            "@@ -40,9 +40,19 @@ public class AuthController {\n-    return ResponseEntity.ok(userService.authenticate(req));\n+    Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));\n+    String token = jwtTokenProvider.generateToken(auth);\n+    return ResponseEntity.ok(new AuthResponse(token));",
            pr42
        ));

        // Seed Demo AI Code Review for PR #42
        CodeReview aiReview = new CodeReview(p1, pr42);
        aiReview.setStatus(ReviewStatus.COMPLETED);
        aiReview.setQualityScore(87.0);
        aiReview.setSummary("AI Code Review Completed: 4 findings identified across 2 files. Code Quality Score: 87.0% (LOW Risk). Flagship Optional.get() check flagged.");

        aiReview.addFinding(new CodeReviewFinding(
            "src/main/java/com/devopsai/smarthealth/service/TelemetryService.java",
            18,
            ReviewCategory.BUG,
            FindingSeverity.HIGH,
            "Potential NoSuchElementException from raw Optional.get()",
            "Direct invocation of repository.findById(id).get() can throw NoSuchElementException at runtime if patient telemetry payload reading is missing.",
            "Use repository.findById(id).orElseThrow(() -> new TelemetryDataNotFoundException(\"Sensor data not found\")) instead of raw .get().",
            "return repository.findById(id).get();",
            aiReview
        ));

        aiReview.addFinding(new CodeReviewFinding(
            "src/main/java/com/devopsai/smarthealth/controller/AuthController.java",
            42,
            ReviewCategory.SECURITY,
            FindingSeverity.MEDIUM,
            "JWT Authentication Token Handler Validation",
            "Ensure JWT access token signing key is injected via environment properties rather than fallback hardcoded string.",
            "Annotate JWT key property with @Value(\"${app.jwt.secret}\") and enforce Minimum 256-bit HMAC key length.",
            "String token = jwtTokenProvider.generateToken(auth);",
            aiReview
        ));

        aiReview.addFinding(new CodeReviewFinding(
            "src/main/java/com/devopsai/smarthealth/service/TelemetryService.java",
            25,
            ReviewCategory.PERFORMANCE,
            FindingSeverity.LOW,
            "Stream Processing Efficiency",
            "Sensor reading map collector creates unnecessary intermediate array instances during high-frequency telemetry bursts.",
            "Use primitive IntStream / DoubleStream or direct list iteration for telemetry sensor streams.",
            "return processReadings(data);",
            aiReview
        ));

        aiReview.addFinding(new CodeReviewFinding(
            "src/main/java/com/devopsai/smarthealth/controller/AuthController.java",
            12,
            ReviewCategory.BEST_PRACTICE,
            FindingSeverity.INFO,
            "Clean Code & Logging Practice",
            "Authentication Controller login entry point correctly uses SLF4J logger context.",
            "Maintain consistent logger usage across diagnostic controllers.",
            "logger.info(\"User login attempt for username: {}\", req.getUsername());",
            aiReview
        ));

        codeReviewRepository.save(aiReview);

        logger.info("Seeding complete. Epics, Sprints, Tasks, Bugs, GitHub Integration (Repo, Commits, PR #42), and AI Code Review (PR #42) created. Demo accounts ready (password: password123): admin, pm_user, dev_user, viewer_user");
    }
}
