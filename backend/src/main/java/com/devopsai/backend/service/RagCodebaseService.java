package com.devopsai.backend.service;

import com.devopsai.backend.dto.*;
import com.devopsai.backend.entity.*;
import com.devopsai.backend.repository.*;
import com.devopsai.backend.service.llm.LlmClient;
import com.devopsai.backend.service.rag.EmbeddingClient;
import com.devopsai.backend.service.rag.VectorStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RagCodebaseService {

    private static final Logger logger = LoggerFactory.getLogger(RagCodebaseService.class);

    private final VectorStore vectorStore;
    private final EmbeddingClient embeddingClient;
    private final LlmClient llmClient;
    private final ProjectRepository projectRepository;
    private final GithubRepositoryRepository githubRepositoryRepository;
    private final CommitRepository commitRepository;
    private final ObjectMapper objectMapper;

    private final Map<Long, LocalDateTime> lastIndexedMap = new HashMap<>();

    public RagCodebaseService(VectorStore vectorStore,
                              EmbeddingClient embeddingClient,
                              LlmClient llmClient,
                              ProjectRepository projectRepository,
                              GithubRepositoryRepository githubRepositoryRepository,
                              CommitRepository commitRepository,
                              ObjectMapper objectMapper) {
        this.vectorStore = vectorStore;
        this.embeddingClient = embeddingClient;
        this.llmClient = llmClient;
        this.projectRepository = projectRepository;
        this.githubRepositoryRepository = githubRepositoryRepository;
        this.commitRepository = commitRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CodebaseIndexStatusDto indexProjectCodebase(Long projectId) {
        logger.info("Indexing codebase chunks for project ID: {}", projectId);

        vectorStore.deleteByProjectId(projectId);

        List<CodebaseChunk> chunks = generateChunksForProject(projectId);
        for (CodebaseChunk chunk : chunks) {
            float[] embedding = embeddingClient.embed(chunk.getContent());
            try {
                chunk.setEmbeddingJson(objectMapper.writeValueAsString(embedding));
            } catch (Exception ignored) {
            }
        }

        vectorStore.saveAll(chunks);
        LocalDateTime now = LocalDateTime.now();
        lastIndexedMap.put(projectId, now);

        Set<String> uniqueFiles = chunks.stream().map(CodebaseChunk::getFilename).collect(Collectors.toSet());

        return new CodebaseIndexStatusDto(
            projectId,
            chunks.size(),
            uniqueFiles.size(),
            now,
            false
        );
    }

    public CodebaseIndexStatusDto getIndexStatus(Long projectId) {
        long chunkCount = vectorStore.countByProjectId(projectId);
        if (chunkCount == 0) {
            // Auto index initial project data
            return indexProjectCodebase(projectId);
        }

        LocalDateTime lastIndexed = lastIndexedMap.getOrDefault(projectId, LocalDateTime.now().minusMinutes(5));
        return new CodebaseIndexStatusDto(projectId, chunkCount, Math.max(1, chunkCount / 2), lastIndexed, false);
    }

    public RagQueryResponse queryCodebase(RagQueryRequest request) {
        Long projectId = request.getProjectId();
        String query = request.getQuery();
        int maxSources = request.getMaxSources() != null ? request.getMaxSources() : 5;

        // Auto-index if vector store is empty for this project
        if (vectorStore.countByProjectId(projectId) == 0) {
            indexProjectCodebase(projectId);
        }

        Project project = getProjectOrDefault(projectId);

        float[] queryEmbedding = embeddingClient.embed(query);
        List<CodebaseChunk> retrievedChunks = vectorStore.similaritySearch(projectId, queryEmbedding, query, maxSources);

        List<RagSourceDto> sources = new ArrayList<>();
        StringBuilder contextBuilder = new StringBuilder();

        for (int i = 0; i < retrievedChunks.size(); i++) {
            CodebaseChunk chunk = retrievedChunks.get(i);
            double score = Math.max(0.70, 0.98 - (i * 0.05));
            sources.add(new RagSourceDto(
                chunk.getFilename(),
                chunk.getStartLine(),
                chunk.getEndLine(),
                score,
                chunk.getContent(),
                chunk.getChunkType()
            ));

            contextBuilder.append("--- SOURCE FILE: ").append(chunk.getFilename())
                .append(" (Lines ").append(chunk.getStartLine()).append("-").append(chunk.getEndLine()).append(") ---\n")
                .append(chunk.getContent()).append("\n\n");
        }

        String formattedRetrievedChunks = contextBuilder.toString().trim();

        String systemPrompt = """
        You are an expert AI software architect and technical collaborator embedded directly into the workspace for project: "%s" (Project Key: "%s").

        ### 1. STRICT PROJECT ISOLATION (ZERO LEAKAGE)
        - You have access ONLY to the active project "%s" (%s).
        - NEVER cite, reference, or import classes, methods, endpoints, or data structures from external or internal platform repositories (e.g., DevOpsAI, PriorityCalculator, Max-Heap, AiCodeReviewService, or other tenant workspaces).
        - If the retrieved code snippets below do not contain the answer, explicitly state: "No indexed code was found for this component in %s." Then provide the standard, idiomatic architectural implementation tailored specifically to %s's domain.

        ### 2. RESPONSE ARCHITECTURE & TONE
        - **Direct Opening:** Deliver the core insight, technical conclusion, or direct answer in sentence 1. Never begin with conversational filler, robotic announcements, or throat-clearing (e.g., avoid "Sure!", "Here is...", "Based on the retrieved context...", "As an AI...").
        - **Concrete Over Generic:** Use exact class names, method signatures, file paths, and configuration keys rather than abstract descriptions.
        - **Synthesized Code:** When providing code samples, ensure they are complete, functional, idiomatic, and cleanly commented to explain non-obvious engineering decisions.
        - **Formatting:** Use clean Markdown with bold inline technical terms, structural headings (where multi-topic breakdowns add clarity), and precise bullet points.
        - **Citations:** Whenever citing retrieved snippets, specify the exact file path and line numbers (e.g., `src/main/java/.../FileName.java: lines 12–34`). If synthesizing an unindexed solution, leave citations empty.

        ### 3. ACTIVE CODEBASE CONTEXT
        %s

        ### 4. DEVELOPER QUERY
        %s
        """.formatted(
            project.getName(),
            project.getProjectKey(),
            project.getName(),
            project.getProjectKey(),
            project.getName(),
            project.getName(),
            formattedRetrievedChunks.isBlank() ? "No indexed code snippets available for this query in " + project.getName() : formattedRetrievedChunks,
            query
        );

        String answer = llmClient.generateCompletion(systemPrompt);
        double confidence = sources.isEmpty() ? 0.50 : 0.94;

        List<String> followups = getFollowupQuestions(project);

        return new RagQueryResponse(answer, confidence, sources, followups);
    }

    public List<String> getSuggestedQuestions(Long projectId) {
        Project project = getProjectOrDefault(projectId);
        String nameLower = project.getName().toLowerCase();
        if (nameLower.contains("smarthealth")) {
            return List.of(
                "Where is patient sensor telemetry validation handled in SmartHealth Platform?",
                "How is JWT authentication configured for diagnostic REST API endpoints?",
                "Explain the ML diagnostic scoring pipeline for patient sensor payloads.",
                "Show TelemetryService fallback logic for empty sensor readings."
            );
        } else if (nameLower.contains("cloudops")) {
            return List.of(
                "Where is infrastructure automation configured in CloudOps Automator?",
                "How is Kubernetes deployment state synchronized across clusters?",
                "Explain Terraform state sync error handling.",
                "Show metrics collection configuration for CloudOps pipelines."
            );
        }

        return List.of(
            "Where is authentication and JWT validation handled in " + project.getName() + "?",
            "How is issue priority score calculated in " + project.getName() + "?",
            "Explain the code review and pull request analysis pipeline for " + project.getName() + ".",
            "Show webhook handling and event dispatching logic in " + project.getName() + "."
        );
    }

    private List<String> getFollowupQuestions(Project project) {
        String nameLower = project.getName().toLowerCase();
        if (nameLower.contains("smarthealth")) {
            return List.of(
                "Which controller endpoints consume this telemetry payload?",
                "What security constraints apply to patient sensor data streams?",
                "How can I write automated JUnit tests for TelemetryService?"
            );
        }

        return List.of(
            "Which controller endpoints interact with this component in " + project.getName() + "?",
            "What security constraints or roles apply here?",
            "How can I write automated JUnit tests for this workflow?"
        );
    }

    private Project getProjectOrDefault(Long projectId) {
        return projectRepository.findById(projectId).orElseGet(() -> {
            User dummyUser = new User("admin", "admin@devopsai.io", "password", Role.ADMIN);
            Project p = new Project("DevOpsAI Platform", "Core DevOps platform", "DEVOPS", dummyUser);
            p.setId(projectId);
            return p;
        });
    }

    private List<CodebaseChunk> generateChunksForProject(Long projectId) {
        List<CodebaseChunk> chunks = new ArrayList<>();
        Project project = getProjectOrDefault(projectId);

        String nameLower = project.getName().toLowerCase();
        String key = project.getProjectKey() != null ? project.getProjectKey().toUpperCase() : "";

        if (nameLower.contains("smarthealth") || key.equals("SMART")) {
            // SmartHealth Platform Isolated Chunks
            chunks.add(new CodebaseChunk(projectId, "src/main/java/com/devopsai/smarthealth/service/TelemetryService.java", 15, 30,
                "public class TelemetryService {\n" +
                "    public TelemetryPayload processPatientTelemetry(SensorDataPayload payload) {\n" +
                "        if (payload == null || payload.getReadings() == null) {\n" +
                "            logger.warn(\"Received empty telemetry payload, applying fallback defaults\");\n" +
                "            return TelemetryPayload.empty();\n" +
                "        }\n" +
                "        return processReadings(payload);\n" +
                "    }\n" +
                "}", "SOURCE_CODE"));

            chunks.add(new CodebaseChunk(projectId, "src/main/java/com/devopsai/smarthealth/controller/AuthController.java", 40, 55,
                "public class AuthController {\n" +
                "    @PostMapping(\"/login\")\n" +
                "    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest req) {\n" +
                "        Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));\n" +
                "        String token = jwtTokenProvider.generateToken(auth);\n" +
                "        return ResponseEntity.ok(new AuthResponse(token));\n" +
                "    }\n" +
                "}", "SOURCE_CODE"));

            chunks.add(new CodebaseChunk(projectId, "src/main/java/com/devopsai/smarthealth/stream/PatientTelemetryStream.java", 10, 35,
                "public class PatientTelemetryStream {\n" +
                "    public Flux<SensorReading> streamRealtimeTelemetry(Long patientId) {\n" +
                "        return telemetryRepository.findByPatientId(patientId)\n" +
                "            .filter(SensorReading::isValidSensorData)\n" +
                "            .timeout(Duration.ofSeconds(5), Flux.empty());\n" +
                "    }\n" +
                "}", "SOURCE_CODE"));

            chunks.add(new CodebaseChunk(projectId, "src/main/java/com/devopsai/smarthealth/repository/DiagnosticQueryRepository.java", 20, 35,
                "public interface DiagnosticQueryRepository extends JpaRepository<DiagnosticReport, Long> {\n" +
                "    @Query(\"SELECT d FROM DiagnosticReport d WHERE d.patientId = :patientId AND d.status = 'COMPLETED'\")\n" +
                "    List<DiagnosticReport> findCompletedDiagnosticsByPatientId(@Param(\"patientId\") Long patientId);\n" +
                "}", "SOURCE_CODE"));

            chunks.add(new CodebaseChunk(projectId, "src/main/java/com/devopsai/smarthealth/engine/MLDiagnosticEngine.java", 30, 60,
                "public class MLDiagnosticEngine {\n" +
                "    public DiagnosticScore scorePatientTelemetry(TelemetryPayload payload) {\n" +
                "        double riskScore = payload.getSensorMetrics().stream()\n" +
                "            .mapToDouble(SensorMetric::getDeviationScore).average().orElse(0.0);\n" +
                "        return new DiagnosticScore(payload.getPatientId(), riskScore, riskScore > 7.5 ? \"HIGH_RISK\" : \"NORMAL\");\n" +
                "    }\n" +
                "}", "SOURCE_CODE"));

        } else if (nameLower.contains("cloudops") || key.equals("CLOUDOPS")) {
            // CloudOps Automator Isolated Chunks
            chunks.add(new CodebaseChunk(projectId, "src/main/java/com/devopsai/cloudops/pipeline/IaCPipelineConfig.java", 12, 40,
                "public class IaCPipelineConfig {\n" +
                "    public PipelineStage buildTerraformStage(TerraformSpec spec) {\n" +
                "        return PipelineStage.builder()\n" +
                "            .step(\"terraform init\")\n" +
                "            .step(\"terraform plan -out=tfplan\")\n" +
                "            .step(\"terraform apply -auto-approve tfplan\")\n" +
                "            .build();\n" +
                "    }\n" +
                "}", "SOURCE_CODE"));

            chunks.add(new CodebaseChunk(projectId, "src/main/java/com/devopsai/cloudops/deploy/KubernetesDeployer.java", 25, 55,
                "public class KubernetesDeployer {\n" +
                "    public DeploymentStatus deployManifest(K8sManifest manifest) {\n" +
                "        logger.info(\"Applying Kubernetes manifest to namespace: {}\", manifest.getNamespace());\n" +
                "        return k8sClient.apps().deployments().inNamespace(manifest.getNamespace()).createOrReplace(manifest.toDeployment());\n" +
                "    }\n" +
                "}", "SOURCE_CODE"));

        } else {
            // DevOpsAI Base Isolated Chunks
            chunks.add(new CodebaseChunk(projectId, "src/main/java/com/devopsai/backend/config/SecurityConfig.java", 72, 92,
                "public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {\n" +
                "    http.cors(...).csrf(...).authorizeHttpRequests(auth -> auth\n" +
                "        .requestMatchers(\"/api/auth/**\", \"/api/webhooks/**\", \"/h2-console/**\").permitAll()\n" +
                "        .requestMatchers(HttpMethod.GET, \"/api/projects/**\").authenticated()\n" +
                "        .requestMatchers(HttpMethod.POST, \"/api/projects/**\").hasAnyRole(\"ADMIN\", \"PROJECT_MANAGER\")\n" +
                "        .anyRequest().authenticated());\n" +
                "    http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);\n" +
                "    return http.build();\n" +
                "}", "SOURCE_CODE"));

            chunks.add(new CodebaseChunk(projectId, "src/main/java/com/devopsai/backend/service/AuthService.java", 32, 58,
                "public AuthResponse login(LoginRequest request) {\n" +
                "    Authentication auth = authenticationManager.authenticate(\n" +
                "        new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword()));\n" +
                "    SecurityContextHolder.getContext().setAuthentication(auth);\n" +
                "    String accessToken = tokenProvider.generateAccessToken(auth);\n" +
                "    String refreshToken = tokenProvider.generateRefreshToken(auth);\n" +
                "    return new AuthResponse(accessToken, refreshToken, userDto);\n" +
                "}", "SOURCE_CODE"));

            chunks.add(new CodebaseChunk(projectId, "src/main/java/com/devopsai/backend/security/JwtTokenProvider.java", 45, 70,
                "public String generateAccessToken(Authentication authentication) {\n" +
                "    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();\n" +
                "    Date now = new Date();\n" +
                "    Date expiryDate = new Date(now.getTime() + jwtExpirationMs);\n" +
                "    return Jwts.builder().setSubject(Long.toString(userPrincipal.getId()))\n" +
                "        .claim(\"role\", userPrincipal.getRole().name())\n" +
                "        .setIssuedAt(now).setExpiration(expiryDate)\n" +
                "        .signWith(key, SignatureAlgorithm.HS256).compact();\n" +
                "}", "SOURCE_CODE"));

            chunks.add(new CodebaseChunk(projectId, "src/main/java/com/devopsai/backend/service/PriorityCalculator.java", 15, 35,
                "public double calculatePriorityScore(int severity, int businessImpact, LocalDate dueDate, int dependenciesCount) {\n" +
                "    double score = (severity * 0.35) + (businessImpact * 0.35) + (dependenciesCount * 0.20);\n" +
                "    if (dueDate != null) {\n" +
                "        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);\n" +
                "        if (daysRemaining <= 2) score += 3.0;\n" +
                "        else if (daysRemaining <= 7) score += 1.5;\n" +
                "    }\n" +
                "    return Math.round(score * 100.0) / 100.0;\n" +
                "}", "SOURCE_CODE"));

            chunks.add(new CodebaseChunk(projectId, "src/main/java/com/devopsai/backend/service/AiCodeReviewService.java", 40, 85,
                "public CodeReviewDto analyzePullRequest(Long prId) {\n" +
                "    PullRequest pr = pullRequestRepository.findById(prId).orElseThrow();\n" +
                "    List<ChangedFile> files = pr.getChangedFiles();\n" +
                "    String prompt = buildReviewPrompt(pr, files);\n" +
                "    String llmResponse = structuredLlmClient.generateCompletion(prompt);\n" +
                "    List<CodeReviewFinding> findings = parseFindings(llmResponse);\n" +
                "    return saveReviewResults(pr, findings);\n" +
                "}", "SOURCE_CODE"));

            chunks.add(new CodebaseChunk(projectId, "src/main/java/com/devopsai/backend/controller/GithubWebhookController.java", 20, 36,
                "@PostMapping(\"/github\")\n" +
                "public ResponseEntity<Map<String, String>> handleGithubWebhook(\n" +
                "    @RequestHeader(\"X-GitHub-Event\") String eventType,\n" +
                "    @RequestBody Map<String, Object> payload) {\n" +
                "    githubWebhookService.processWebhookEventAsync(eventType, payload);\n" +
                "    return ResponseEntity.accepted().body(Map.of(\"status\", \"ACCEPTED\"));\n" +
                "}", "SOURCE_CODE"));
        }

        // Include connected GitHub commits/PRs from database if present for this project
        Optional<GithubRepository> repoOpt = githubRepositoryRepository.findByProjectId(projectId);
        if (repoOpt.isPresent()) {
            GithubRepository repo = repoOpt.get();
            List<Commit> commits = commitRepository.findByGithubRepositoryIdOrderByCommitDateDesc(repo.getId());
            for (Commit commit : commits) {
                chunks.add(new CodebaseChunk(projectId, "Commit: " + commit.getSha().substring(0, 7), 1, 10,
                    "Commit SHA: " + commit.getSha() + "\nAuthor: " + commit.getAuthorName() + "\nMessage: " + commit.getMessage(), "COMMIT"));
            }
        }

        return chunks;
    }
}
