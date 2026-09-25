package com.devopsai.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/oauth/github")
public class GithubOAuthController {

    /**
     * GitHub OAuth Callback Endpoint Stub.
     * Receives authorization code from GitHub OAuth consent screen exchange.
     */
    @GetMapping("/callback")
    public ResponseEntity<Map<String, Object>> handleOAuthCallback(@RequestParam(name = "code") String code,
                                                                   @RequestParam(name = "state", required = false) String state) {
        Map<String, Object> response = Map.of(
            "status", "SUCCESS",
            "message", "GitHub OAuth authorization code exchanged successfully.",
            "authorizationCode", code,
            "tokenType", "bearer",
            "scope", "repo,read:user,user:email"
        );
        return ResponseEntity.ok(response);
    }
}
