package com.devopsai.backend.service;

import com.devopsai.backend.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GithubApiClient {

    private static final Logger logger = LoggerFactory.getLogger(GithubApiClient.class);
    private final RestTemplate restTemplate;

    public GithubApiClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Fetches commits from GitHub API, with fallback to demo commits if offline/no token.
     */
    @SuppressWarnings("unchecked")
    public List<Commit> fetchCommits(GithubRepository repo) {
        List<Commit> commits = new ArrayList<>();
        if (repo.getAccessToken() != null && !repo.getAccessToken().isBlank()) {
            try {
                String url = String.format("https://api.github.com/repos/%s/%s/commits?per_page=10",
                    repo.getOwnerName(), repo.getRepoName());

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + repo.getAccessToken());
                headers.set("Accept", "application/vnd.github+json");

                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ParameterizedTypeReference<List<Map<String, Object>>> typeRef = new ParameterizedTypeReference<>() {};
                ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(url, HttpMethod.GET, entity, typeRef);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    for (Map<String, Object> map : response.getBody()) {
                        String sha = (String) map.get("sha");
                        String htmlUrl = (String) map.get("html_url");
                        Map<String, Object> commitMap = (Map<String, Object>) map.get("commit");
                        String message = commitMap != null ? (String) commitMap.get("message") : "Commit update";

                        Map<String, Object> authorMap = commitMap != null ? (Map<String, Object>) commitMap.get("author") : null;
                        String authorName = authorMap != null ? (String) authorMap.get("name") : repo.getOwnerName();
                        String authorEmail = authorMap != null ? (String) authorMap.get("email") : "dev@devopsai.io";

                        commits.add(new Commit(sha, message, authorName, authorEmail, LocalDateTime.now(), htmlUrl, repo));
                    }
                    return commits;
                }
            } catch (Exception e) {
                logger.warn("GitHub API live fetch failed for commits: {}. Falling back to demo data.", e.getMessage());
            }
        }

        // Demo Fallback Commits
        commits.add(new Commit("a1b2c3d4e5f678901234567890abcdef12345678", "feat: Add telemetry stream pipeline and security filter", "pm_user", "pm@devopsai.io", LocalDateTime.now().minusHours(2), repo.getHtmlUrl(), repo));
        commits.add(new Commit("b2c3d4e5f678901234567890abcdef123456789a", "fix: Resolve memory leak in patient sensor listener", "dev_user", "dev@devopsai.io", LocalDateTime.now().minusDays(1), repo.getHtmlUrl(), repo));
        commits.add(new Commit("c3d4e5f678901234567890abcdef123456789ab2", "chore: Update Spring Security JWT dependencies", "admin", "admin@devopsai.io", LocalDateTime.now().minusDays(2), repo.getHtmlUrl(), repo));
        return commits;
    }

    /**
     * Fetches Pull Requests from GitHub API, with fallback to demo PRs if offline/no token.
     */
    @SuppressWarnings("unchecked")
    public List<PullRequest> fetchPullRequests(GithubRepository repo) {
        List<PullRequest> prs = new ArrayList<>();
        if (repo.getAccessToken() != null && !repo.getAccessToken().isBlank()) {
            try {
                String url = String.format("https://api.github.com/repos/%s/%s/pulls?state=all&per_page=5",
                    repo.getOwnerName(), repo.getRepoName());

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + repo.getAccessToken());
                headers.set("Accept", "application/vnd.github+json");

                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ParameterizedTypeReference<List<Map<String, Object>>> typeRef = new ParameterizedTypeReference<>() {};
                ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(url, HttpMethod.GET, entity, typeRef);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    for (Map<String, Object> map : response.getBody()) {
                        int number = (Integer) map.get("number");
                        String title = (String) map.get("title");
                        String body = (String) map.get("body");
                        String stateStr = (String) map.get("state");

                        PullRequestState state = "closed".equalsIgnoreCase(stateStr) ? PullRequestState.CLOSED : PullRequestState.OPEN;
                        if (map.get("merged_at") != null) {
                            state = PullRequestState.MERGED;
                        }

                        Map<String, Object> headMap = (Map<String, Object>) map.get("head");
                        String headBranch = headMap != null ? (String) headMap.get("ref") : "feature/auth";

                        Map<String, Object> baseMap = (Map<String, Object>) map.get("base");
                        String baseBranch = baseMap != null ? (String) baseMap.get("ref") : "main";

                        Map<String, Object> userMap = (Map<String, Object>) map.get("user");
                        String author = userMap != null ? (String) userMap.get("login") : "dev_user";

                        prs.add(new PullRequest(number, title, body, state, headBranch, baseBranch, author, 3, 142, 38, repo));
                    }
                    return prs;
                }
            } catch (Exception e) {
                logger.warn("GitHub API live fetch failed for PRs: {}. Falling back to demo PR payload.", e.getMessage());
            }
        }

        // Demo Fallback PR #42
        PullRequest pr42 = new PullRequest(
            42,
            "PR #42: Feature 1 & 2 Core Integration - Auth, RBAC & Priority Queue",
            "Implements JWT Authentication, Role-Based Access Control, and DSA Max-Heap Priority Queue algorithm for issues.",
            PullRequestState.OPEN,
            "feature/auth-rbac-priority",
            "main",
            "dev_user",
            3,
            285,
            42,
            repo
        );
        prs.add(pr42);

        PullRequest pr41 = new PullRequest(
            41,
            "PR #41: PostgreSQL & Redis Docker Compose Setup",
            "Containerizes PostgreSQL 16 and Redis 7 in Infrastructure docker-compose.yml.",
            PullRequestState.MERGED,
            "feature/docker-compose",
            "main",
            "admin",
            2,
            75,
            12,
            repo
        );
        prs.add(pr41);

        return prs;
    }

    /**
     * Generates demo changed files & diff patch for PR #42.
     */
    public List<ChangedFile> fetchChangedFiles(PullRequest pullRequest) {
        List<ChangedFile> files = new ArrayList<>();

        files.add(new ChangedFile(
            "src/main/java/com/devopsai/backend/controller/AuthController.java",
            "modified",
            45,
            10,
            "@@ -15,10 +15,15 @@ public class AuthController {\n+    @PostMapping(\"/login\")\n+    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {\n+        return ResponseEntity.ok(authService.login(request));\n+    }",
            pullRequest
        ));

        files.add(new ChangedFile(
            "src/main/java/com/devopsai/backend/service/PriorityCalculator.java",
            "added",
            120,
            0,
            "@@ -0,0 +1,120 @@\n+package com.devopsai.backend.service;\n+\n+@Service\n+public class PriorityCalculator {\n+    // Max-Heap PriorityQueue drainage algorithm\n+}",
            pullRequest
        ));

        files.add(new ChangedFile(
            "pom.xml",
            "modified",
            12,
            2,
            "@@ -52,7 +52,7Dependency>\n+            <groupId>com.h2database</groupId>\n+            <artifactId>h2</artifactId>\n+            <scope>runtime</scope>\n+        </dependency>",
            pullRequest
        ));

        return files;
    }

    /**
     * Dynamically fetches repository source/config files for security scanning via GitHub API
     * or project-specific temporary workspace.
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> fetchRepositoryFiles(GithubRepository repo) {
        Map<String, String> filesMap = new java.util.HashMap<>();
        if (repo == null) {
            return filesMap;
        }

        // 1. Fetch via GitHub API if access token and owner/repo are present
        if (repo.getAccessToken() != null && !repo.getAccessToken().isBlank()
                && repo.getOwnerName() != null && !repo.getOwnerName().isBlank()
                && repo.getRepoName() != null && !repo.getRepoName().isBlank()) {
            try {
                String branch = repo.getDefaultBranch() != null ? repo.getDefaultBranch() : "main";
                String treeUrl = String.format("https://api.github.com/repos/%s/%s/git/trees/%s?recursive=1",
                        repo.getOwnerName(), repo.getRepoName(), branch);

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + repo.getAccessToken());
                headers.set("Accept", "application/vnd.github+json");
                HttpEntity<Void> entity = new HttpEntity<>(headers);

                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                        treeUrl, HttpMethod.GET, entity, new ParameterizedTypeReference<Map<String, Object>>() {});

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    List<Map<String, Object>> tree = (List<Map<String, Object>>) response.getBody().get("tree");
                    if (tree != null) {
                        int count = 0;
                        for (Map<String, Object> item : tree) {
                            if (count >= 100) break; // Limit file count for scan
                            String type = (String) item.get("type");
                            String path = (String) item.get("path");
                            if ("blob".equals(type) && path != null && isScannableFile(path)) {
                                String content = fetchSingleFileContent(repo, path);
                                if (content != null) {
                                    filesMap.put(path, content);
                                    count++;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("GitHub API file tree fetch failed for {}/{}: {}", repo.getOwnerName(), repo.getRepoName(), e.getMessage());
            }
        }

        // 2. Scan isolated project-specific temporary workspace (/tmp/repos/{projectId}/ or /tmp/scans/{projectId}/)
        if (repo.getProject() != null && repo.getProject().getId() != null) {
            Long projectId = repo.getProject().getId();
            List<java.io.File> candidates = List.of(
                    new java.io.File("/tmp/repos/" + projectId),
                    new java.io.File("/tmp/scans/" + projectId),
                    new java.io.File(System.getProperty("java.io.tmpdir"), "repos/" + projectId),
                    new java.io.File(System.getProperty("java.io.tmpdir"), "scans/" + projectId)
            );
            for (java.io.File tempDir : candidates) {
                if (tempDir.exists() && tempDir.isDirectory()) {
                    readFilesRecursively(tempDir, tempDir.getAbsolutePath(), filesMap);
                }
            }
        }

        return filesMap;
    }

    private String fetchSingleFileContent(GithubRepository repo, String path) {
        try {
            String branch = repo.getDefaultBranch() != null ? repo.getDefaultBranch() : "main";
            String url = String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s",
                    repo.getOwnerName(), repo.getRepoName(), path, branch);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + repo.getAccessToken());
            headers.set("Accept", "application/vnd.github.v3.raw");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (Exception e) {
            logger.debug("Failed to fetch content for file {}: {}", path, e.getMessage());
        }
        return null;
    }

    private boolean isScannableFile(String path) {
        if (path == null) return false;
        String lower = path.toLowerCase();
        if (lower.contains("/target/") || lower.contains("/node_modules/") || lower.contains("/.git/") || lower.contains("/dist/") || lower.contains("/build/")) {
            return false;
        }
        return lower.endsWith(".java") || lower.endsWith(".js") || lower.endsWith(".jsx") || lower.endsWith(".ts")
                || lower.endsWith(".tsx") || lower.endsWith(".xml") || lower.endsWith(".json") || lower.endsWith(".properties")
                || lower.endsWith(".yml") || lower.endsWith(".yaml") || lower.endsWith(".py") || lower.endsWith(".go")
                || lower.endsWith(".sql");
    }

    private void readFilesRecursively(java.io.File dir, String rootPath, Map<String, String> files) {
        java.io.File[] list = dir.listFiles();
        if (list == null) return;
        for (java.io.File f : list) {
            if (f.isDirectory()) {
                if (!f.getName().equalsIgnoreCase("target") && !f.getName().equalsIgnoreCase("node_modules")
                        && !f.getName().equalsIgnoreCase(".git") && !f.getName().equalsIgnoreCase("build")) {
                    readFilesRecursively(f, rootPath, files);
                }
            } else if (f.isFile() && isScannableFile(f.getAbsolutePath())) {
                try {
                    String relativePath = f.getAbsolutePath().substring(rootPath.length()).replace('\\', '/');
                    if (relativePath.startsWith("/")) relativePath = relativePath.substring(1);
                    String content = java.nio.file.Files.readString(f.toPath());
                    files.put(relativePath, content);
                } catch (Exception ignored) {
                }
            }
        }
    }
}

