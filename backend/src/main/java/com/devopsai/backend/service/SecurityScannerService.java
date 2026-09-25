package com.devopsai.backend.service;

import com.devopsai.backend.dto.SecurityFindingDto;
import com.devopsai.backend.dto.SecurityScanReportDto;
import com.devopsai.backend.dto.TriggerSecurityScanRequest;
import com.devopsai.backend.entity.*;
import com.devopsai.backend.exception.ResourceNotFoundException;
import com.devopsai.backend.repository.GithubRepositoryRepository;
import com.devopsai.backend.repository.ProjectRepository;
import com.devopsai.backend.repository.SecurityScanReportRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SecurityScannerService {

    private final SecurityScanReportRepository reportRepository;
    private final ProjectRepository projectRepository;
    private final GithubRepositoryRepository githubRepositoryRepository;
    private final GithubApiClient githubApiClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public SecurityScannerService(SecurityScanReportRepository reportRepository,
                                  ProjectRepository projectRepository,
                                  GithubRepositoryRepository githubRepositoryRepository,
                                  GithubApiClient githubApiClient,
                                  ObjectMapper objectMapper) {
        this.reportRepository = reportRepository;
        this.projectRepository = projectRepository;
        this.githubRepositoryRepository = githubRepositoryRepository;
        this.githubApiClient = githubApiClient;
        this.objectMapper = objectMapper;
    }

    public SecurityScannerService(SecurityScanReportRepository reportRepository,
                                  ProjectRepository projectRepository,
                                  ObjectMapper objectMapper) {
        this(reportRepository, projectRepository, null, new GithubApiClient(), objectMapper);
    }

    @Transactional
    public SecurityScanReportDto runSecurityScan(Long projectId, TriggerSecurityScanRequest request) {
        // 1. Enforce Dynamic Project Scoping
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }

        // Verify that project has a linked GitHub repository URL and Access Token
        Optional<GithubRepository> repoOpt = githubRepositoryRepository != null
                ? githubRepositoryRepository.findByProjectId(projectId)
                : Optional.empty();

        if (repoOpt.isEmpty()) {
            throw new IllegalArgumentException("Repository not connected for project: " + projectId);
        }

        GithubRepository repo = repoOpt.get();
        boolean hasUrl = (repo.getHtmlUrl() != null && !repo.getHtmlUrl().isBlank())
                || (repo.getOwnerName() != null && !repo.getOwnerName().isBlank() && repo.getRepoName() != null && !repo.getRepoName().isBlank());
        boolean hasToken = repo.getAccessToken() != null && !repo.getAccessToken().isBlank();

        if (!hasUrl || !hasToken) {
            throw new IllegalArgumentException("Repository not connected for project: " + projectId);
        }

        String commitSha = (request != null && request.getCommitSha() != null && !request.getCommitSha().isBlank())
                ? request.getCommitSha() : "c0de98765432";
        boolean simulateVulnerabilities = (request != null && request.isSimulateVulnerabilities()) || commitSha.toLowerCase().contains("vuln");

        // 2. Fetch target repository files dynamically via GitHub API or isolated workspace
        Map<String, String> files = githubApiClient != null ? githubApiClient.fetchRepositoryFiles(repo) : new HashMap<>();

        SecurityScanReport report = new SecurityScanReport(projectId, commitSha);

        // 3. Dynamic Rule Engine Execution against target repo files
        List<SecurityFindingDto> findings = new ArrayList<>();
        findings.addAll(runSecretDetector(files, simulateVulnerabilities));
        findings.addAll(runSqlInjDetector(files, simulateVulnerabilities));
        findings.addAll(runAuthChecker(files, simulateVulnerabilities));
        findings.addAll(runInsecureApiDetector(files, simulateVulnerabilities));
        findings.addAll(runCveScanner(files, simulateVulnerabilities));

        // Aggregate severity counts
        int critical = (int) findings.stream().filter(f -> f.getSeverity() == SecuritySeverity.CRITICAL).count();
        int high = (int) findings.stream().filter(f -> f.getSeverity() == SecuritySeverity.HIGH).count();
        int medium = (int) findings.stream().filter(f -> f.getSeverity() == SecuritySeverity.MEDIUM).count();
        int low = (int) findings.stream().filter(f -> f.getSeverity() == SecuritySeverity.LOW).count();

        // Calculate overallRiskLevel:
        // LOW: 0 Critical and 0 High
        // MEDIUM: 0 Critical and <= 2 High
        // HIGH: 1 Critical or >= 3 High
        // CRITICAL: > 1 Critical
        SecurityRiskLevel riskLevel;
        if (critical > 1) {
            riskLevel = SecurityRiskLevel.CRITICAL;
        } else if (critical == 1 || high >= 3) {
            riskLevel = SecurityRiskLevel.HIGH;
        } else if (critical == 0 && high <= 2 && high > 0) {
            riskLevel = SecurityRiskLevel.MEDIUM;
        } else {
            riskLevel = SecurityRiskLevel.LOW;
        }

        // Calculate overall Security Score: 100 - (25*Critical + 15*High + 5*Medium + 1*Low)
        double score = Math.max(0.0, 100.0 - (critical * 25.0 + high * 15.0 + medium * 5.0 + low * 1.0));

        report.setCriticalCount(critical);
        report.setHighCount(high);
        report.setMediumCount(medium);
        report.setLowCount(low);
        report.setOverallRiskLevel(riskLevel);
        report.setOverallSecurityScore(score);
        report.setStatus((critical > 0 || high > 0) ? "FAILED" : (medium > 0 || low > 0) ? "WARNINGS" : "PASSED");

        try {
            report.setFindingsJson(objectMapper.writeValueAsString(findings));
        } catch (JsonProcessingException e) {
            report.setFindingsJson("[]");
        }

        SecurityScanReport savedReport = reportRepository.save(report);
        return toDto(savedReport);
    }

    @Transactional(readOnly = true)
    public List<SecurityScanReportDto> getReportsForProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        return reportRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SecurityScanReportDto getLatestReportForProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        Optional<SecurityScanReport> latestOpt = reportRepository.findFirstByProjectIdOrderByCreatedAtDesc(projectId);
        if (latestOpt.isEmpty()) {
            return runSecurityScan(projectId, new TriggerSecurityScanRequest(projectId, "head", false));
        }
        return toDto(latestOpt.get());
    }

    @Transactional(readOnly = true)
    public SecurityScanReportDto getReportById(Long reportId) {
        SecurityScanReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Security report not found with id: " + reportId));
        return toDto(report);
    }

    // Detector 1: SecretDetector (Hardcoded Secrets: sk-proj-, ghp_, AKIA..., private keys)
    private List<SecurityFindingDto> runSecretDetector(Map<String, String> files, boolean simulateVulnerabilities) {
        List<SecurityFindingDto> findings = new ArrayList<>();
        int idCounter = 1;

        Pattern openaiPattern = Pattern.compile("sk-proj-[A-Za-z0-9_-]+");
        Pattern ghpPattern = Pattern.compile("ghp_[A-Za-z0-9]{36}");
        Pattern awsPattern = Pattern.compile("AKIA[0-9A-Z]{16}");
        Pattern pkeyPattern = Pattern.compile("-----BEGIN (?:RSA|EC|DSA|OPENSSH|PRIVATE) KEY-----");

        for (Map.Entry<String, String> entry : files.entrySet()) {
            String filePath = entry.getKey();
            String content = entry.getValue();
            String[] lines = content.split("\\r?\\n");

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                int lineNum = i + 1;

                if (openaiPattern.matcher(line).find()) {
                    findings.add(new SecurityFindingDto(
                            "SEC-SEC-" + (idCounter++),
                            "Hardcoded OpenAI API Secret Key Detected",
                            "A plain text OpenAI API key starting with 'sk-proj-...' was detected in " + filePath + ".",
                            SecuritySeverity.CRITICAL,
                            SecurityScanCategory.HARDCODED_SECRETS,
                            filePath,
                            lineNum,
                            line.trim(),
                            "Move sensitive secrets to environment variables or secret manager (e.g. System.getenv(\"OPENAI_API_KEY\")).",
                            null
                    ));
                } else if (ghpPattern.matcher(line).find()) {
                    findings.add(new SecurityFindingDto(
                            "SEC-SEC-" + (idCounter++),
                            "Hardcoded GitHub Personal Access Token Detected",
                            "A plain text GitHub PAT starting with 'ghp_' was detected in " + filePath + ".",
                            SecuritySeverity.CRITICAL,
                            SecurityScanCategory.HARDCODED_SECRETS,
                            filePath,
                            lineNum,
                            line.trim(),
                            "Revoke the leaked token and store secrets in environment variables.",
                            null
                    ));
                } else if (awsPattern.matcher(line).find()) {
                    findings.add(new SecurityFindingDto(
                            "SEC-SEC-" + (idCounter++),
                            "Hardcoded AWS Access Key ID Detected",
                            "An AWS Access Key ID (AKIA...) was detected in " + filePath + ".",
                            SecuritySeverity.CRITICAL,
                            SecurityScanCategory.HARDCODED_SECRETS,
                            filePath,
                            lineNum,
                            line.trim(),
                            "Remove hardcoded AWS credentials and use AWS IAM roles or environment variables.",
                            null
                    ));
                } else if (pkeyPattern.matcher(line).find()) {
                    findings.add(new SecurityFindingDto(
                            "SEC-SEC-" + (idCounter++),
                            "Hardcoded Private Key Block Detected",
                            "An unencrypted private key header was detected in " + filePath + ".",
                            SecuritySeverity.CRITICAL,
                            SecurityScanCategory.HARDCODED_SECRETS,
                            filePath,
                            lineNum,
                            line.trim(),
                            "Do not commit private key files or key blocks to source control.",
                            null
                    ));
                }
            }
        }

        if (simulateVulnerabilities && findings.isEmpty()) {
            findings.add(new SecurityFindingDto(
                    "SEC-001",
                    "Hardcoded OpenAI API Secret Key Detected",
                    "A plain text API key starting with 'sk-proj-...' was detected in source file config/LlmConfig.java.",
                    SecuritySeverity.CRITICAL,
                    SecurityScanCategory.HARDCODED_SECRETS,
                    "src/main/java/com/devopsai/backend/config/LlmConfig.java",
                    24,
                    "private static final String API_KEY = \"sk-proj-9821384791823912839\";",
                    "Move sensitive secrets to environment variables or Java system properties: System.getenv(\"OPENAI_API_KEY\").",
                    null
            ));
        }

        return findings;
    }

    // Detector 2: SqlInjDetector (SQL Injection Concatenation Rule: stmt.executeQuery("..." + var))
    private List<SecurityFindingDto> runSqlInjDetector(Map<String, String> files, boolean simulateVulnerabilities) {
        List<SecurityFindingDto> findings = new ArrayList<>();
        int idCounter = 1;

        Pattern sqlConcatenationPattern = Pattern.compile("(?:executeQuery|executeUpdate|createNativeQuery|createQuery|db\\.query)\\s*\\(\\s*\"[^\"]*\"\\s*\\+");
        Pattern rawSqlSelectConcat = Pattern.compile("SELECT\\s+.*?\\s+FROM\\s+.*?\\s+WHERE\\s+.*?\"\\s*\\+");

        for (Map.Entry<String, String> entry : files.entrySet()) {
            String filePath = entry.getKey();
            String content = entry.getValue();
            String[] lines = content.split("\\r?\\n");

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                int lineNum = i + 1;

                if (sqlConcatenationPattern.matcher(line).find() || rawSqlSelectConcat.matcher(line).find()) {
                    findings.add(new SecurityFindingDto(
                            "SEC-SQL-" + (idCounter++),
                            "Raw SQL String Concatenation Vulnerability",
                            "Direct unescaped query string concatenation detected in " + filePath + ".",
                            SecuritySeverity.HIGH,
                            SecurityScanCategory.SQL_INJECTION,
                            filePath,
                            lineNum,
                            line.trim(),
                            "Refactor to use parameterized JPA CriteriaBuilder, PreparedStatement, or Spring Data JPA @Query with :param bindings.",
                            "CWE-89"
                    ));
                }
            }
        }

        if (simulateVulnerabilities && findings.isEmpty()) {
            findings.add(new SecurityFindingDto(
                    "SEC-003",
                    "Raw SQL String Concatenation Vulnerability",
                    "Direct unescaped query string concatenation detected inside CustomReportDao.java.",
                    SecuritySeverity.HIGH,
                    SecurityScanCategory.SQL_INJECTION,
                    "src/main/java/com/devopsai/backend/dao/CustomReportDao.java",
                    58,
                    "String query = \"SELECT * FROM users WHERE username = '\" + userInput + \"'\";",
                    "Refactor to use parameterized JPA CriteriaBuilder or Spring Data JPA `@Query` with `:username` parameters.",
                    "CWE-89"
            ));
        }

        return findings;
    }

    // Detector 3: AuthChecker (Weak Auth & RBAC Evaluator)
    private List<SecurityFindingDto> runAuthChecker(Map<String, String> files, boolean simulateVulnerabilities) {
        List<SecurityFindingDto> findings = new ArrayList<>();
        int idCounter = 1;

        for (Map.Entry<String, String> entry : files.entrySet()) {
            String filePath = entry.getKey();
            String content = entry.getValue();

            if (filePath.endsWith(".java") && (filePath.contains("Controller") || content.contains("@RestController") || content.contains("@Controller"))) {
                String[] lines = content.split("\\r?\\n");
                boolean classHasPreAuthorize = content.contains("@PreAuthorize");

                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    int lineNum = i + 1;

                    if ((line.contains("@PostMapping") || line.contains("@DeleteMapping") || line.contains("@PutMapping"))
                            && (line.contains("/admin") || line.contains("/reset") || line.contains("/delete") || line.contains("/config"))) {

                        boolean methodHasPreAuthorize = false;
                        for (int k = Math.max(0, i - 3); k < i; k++) {
                            if (lines[k].contains("@PreAuthorize") || lines[k].contains("@Secured")) {
                                methodHasPreAuthorize = true;
                                break;
                            }
                        }
                        if (!methodHasPreAuthorize && !classHasPreAuthorize) {
                            findings.add(new SecurityFindingDto(
                                    "SEC-AUTH-" + (idCounter++),
                                    "Missing Method Level Security Annotation on Admin Endpoint",
                                    "Endpoint handling sensitive operations in " + filePath + " lacks `@PreAuthorize` role validation.",
                                    SecuritySeverity.HIGH,
                                    SecurityScanCategory.WEAK_AUTHENTICATION,
                                    filePath,
                                    lineNum,
                                    line.trim(),
                                    "Add `@PreAuthorize(\"hasRole('ADMIN')\")` to restrict execution to authorized administrators.",
                                    "CWE-285"
                            ));
                        }
                    }
                }
            }
        }

        if (simulateVulnerabilities && findings.isEmpty()) {
            findings.add(new SecurityFindingDto(
                    "SEC-004",
                    "Missing Method Level Security Annotation on Admin Endpoint",
                    "Endpoint `@PostMapping(\"/admin/reset\")` lacks `@PreAuthorize` project scoping or role validation.",
                    SecuritySeverity.HIGH,
                    SecurityScanCategory.WEAK_AUTHENTICATION,
                    "src/main/java/com/devopsai/backend/controller/AdminController.java",
                    31,
                    "@PostMapping(\"/admin/reset\") public ResponseEntity<Void> resetData()",
                    "Add `@PreAuthorize(\"hasRole('ADMIN')\")` to restrict execution to authorized administrators.",
                    "CWE-285"
            ));
        }

        return findings;
    }

    // Detector 4: InsecureApiDetector (Missing @Valid or unprotected endpoints)
    private List<SecurityFindingDto> runInsecureApiDetector(Map<String, String> files, boolean simulateVulnerabilities) {
        List<SecurityFindingDto> findings = new ArrayList<>();
        int idCounter = 1;

        for (Map.Entry<String, String> entry : files.entrySet()) {
            String filePath = entry.getKey();
            String content = entry.getValue();

            if (filePath.endsWith(".java") && (filePath.contains("Controller") || content.contains("@RestController") || content.contains("@Controller"))) {
                String[] lines = content.split("\\r?\\n");
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    int lineNum = i + 1;

                    if (line.contains("@PostMapping") || line.contains("@PutMapping") || line.contains("@PatchMapping")) {
                        String methodSig = line + (i + 1 < lines.length ? " " + lines[i+1] : "");
                        if (methodSig.contains("@RequestBody") && !methodSig.contains("@Valid") && !methodSig.contains("@Validated")) {
                            findings.add(new SecurityFindingDto(
                                    "SEC-API-" + (idCounter++),
                                    "Missing `@Valid` Input Body Validation on Sensitive Endpoint",
                                    "`@RequestBody` is passed to controller in " + filePath + " without `@Valid` annotation.",
                                    SecuritySeverity.MEDIUM,
                                    SecurityScanCategory.INSECURE_API,
                                    filePath,
                                    lineNum,
                                    line.trim(),
                                    "Annotate body with `@Valid` and add JSR-380 validation rules (`@NotNull`, `@Size`).",
                                    "CWE-20"
                            ));
                        }
                    }
                }
            }
        }

        if (simulateVulnerabilities && findings.isEmpty()) {
            findings.add(new SecurityFindingDto(
                    "SEC-005",
                    "Missing `@Valid` Input Body Validation on Sensitive Endpoint",
                    "`@RequestBody UserDto dto` is passed to controller without `@Valid` annotation, allowing null/blank inputs.",
                    SecuritySeverity.MEDIUM,
                    SecurityScanCategory.INSECURE_API,
                    "src/main/java/com/devopsai/backend/controller/UserController.java",
                    45,
                    "public ResponseEntity<UserDto> updateUser(@RequestBody UserDto dto)",
                    "Annotate body with `@Valid` and add JSR-380 validation rules (`@NotNull`, `@Size`).",
                    "CWE-20"
            ));
        }

        return findings;
    }

    // Detector 5: CveScanner (Dependency CVE & pom.xml / package.json Analyzer)
    private List<SecurityFindingDto> runCveScanner(Map<String, String> files, boolean simulateVulnerabilities) {
        List<SecurityFindingDto> findings = new ArrayList<>();
        int idCounter = 1;

        for (Map.Entry<String, String> entry : files.entrySet()) {
            String filePath = entry.getKey();
            String content = entry.getValue();

            if (filePath.endsWith("pom.xml")) {
                if (content.contains("log4j-core") && (content.contains("2.14.") || content.contains("2.15.0"))) {
                    findings.add(new SecurityFindingDto(
                            "SEC-CVE-" + (idCounter++),
                            "Known Vulnerable Dependency Detected: Log4j",
                            "Dependency log4j-core is vulnerable to Remote Code Execution (RCE).",
                            SecuritySeverity.CRITICAL,
                            SecurityScanCategory.DEPENDENCY_CVE,
                            filePath,
                            findLineNumber(content, "log4j-core"),
                            extractSnippet(content, "log4j-core"),
                            "Upgrade Log4j to version 2.17.1 or higher immediately.",
                            "CVE-2021-44228"
                    ));
                }
                if (content.contains("spring-core") && content.contains("5.3.14")) {
                    findings.add(new SecurityFindingDto(
                            "SEC-CVE-" + (idCounter++),
                            "Vulnerable Spring Framework RCE (Spring4Shell)",
                            "Dependency spring-core 5.3.14 is vulnerable to Spring4Shell RCE.",
                            SecuritySeverity.CRITICAL,
                            SecurityScanCategory.DEPENDENCY_CVE,
                            filePath,
                            findLineNumber(content, "spring-core"),
                            extractSnippet(content, "spring-core"),
                            "Upgrade Spring Framework to 5.3.18+ or 5.2.20+.",
                            "CVE-2022-22965"
                    ));
                }
            } else if (filePath.endsWith("package.json")) {
                if (content.contains("\"axios\"") && (content.contains("0.21.1") || content.contains("0.21.0"))) {
                    findings.add(new SecurityFindingDto(
                            "SEC-CVE-" + (idCounter++),
                            "Outdated Minor Dependency: Axios 0.21.1",
                            "Frontend dependency axios 0.21.1 contains low severity SSRF vulnerability.",
                            SecuritySeverity.LOW,
                            SecurityScanCategory.DEPENDENCY_CVE,
                            filePath,
                            findLineNumber(content, "\"axios\""),
                            extractSnippet(content, "\"axios\""),
                            "Upgrade axios to version 1.7.0 or higher.",
                            "CVE-2020-28168"
                    ));
                }
                if (content.contains("\"lodash\"") && content.contains("4.17.19")) {
                    findings.add(new SecurityFindingDto(
                            "SEC-CVE-" + (idCounter++),
                            "Vulnerable Lodash Dependency (Prototype Pollution)",
                            "Dependency lodash 4.17.19 contains Prototype Pollution vulnerability.",
                            SecuritySeverity.HIGH,
                            SecurityScanCategory.DEPENDENCY_CVE,
                            filePath,
                            findLineNumber(content, "\"lodash\""),
                            extractSnippet(content, "\"lodash\""),
                            "Upgrade lodash to 4.17.21 or higher.",
                            "CVE-2021-23337"
                    ));
                }
            }
        }

        if (simulateVulnerabilities && findings.isEmpty()) {
            findings.add(new SecurityFindingDto(
                    "SEC-006",
                    "Known Vulnerable Dependency Detected: Log4j 2.14.1",
                    "Dependency log4j-core 2.14.1 is vulnerable to Remote Code Execution (RCE).",
                    SecuritySeverity.CRITICAL,
                    SecurityScanCategory.DEPENDENCY_CVE,
                    "pom.xml",
                    112,
                    "<dependency><groupId>org.apache.logging.log4j</groupId><artifactId>log4j-core</artifactId><version>2.14.1</version></dependency>",
                    "Upgrade Log4j to version 2.17.1 or higher immediately.",
                    "CVE-2021-44228"
            ));
        }

        return findings;
    }

    private int findLineNumber(String content, String target) {
        String[] lines = content.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(target)) {
                return i + 1;
            }
        }
        return 1;
    }

    private String extractSnippet(String content, String target) {
        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            if (line.contains(target)) {
                return line.trim();
            }
        }
        return target;
    }

    public SecurityScanReportDto toDto(SecurityScanReport report) {
        List<SecurityFindingDto> findings;
        try {
            if (report.getFindingsJson() != null && !report.getFindingsJson().isBlank()) {
                findings = objectMapper.readValue(report.getFindingsJson(), new TypeReference<List<SecurityFindingDto>>() {});
            } else {
                findings = new ArrayList<>();
            }
        } catch (Exception e) {
            findings = new ArrayList<>();
        }

        return new SecurityScanReportDto(
                report.getId(),
                report.getProjectId(),
                report.getScannedCommitSha(),
                report.getStatus(),
                report.getOverallRiskLevel(),
                report.getCriticalCount(),
                report.getHighCount(),
                report.getMediumCount(),
                report.getLowCount(),
                report.getOverallSecurityScore(),
                findings,
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }
}
