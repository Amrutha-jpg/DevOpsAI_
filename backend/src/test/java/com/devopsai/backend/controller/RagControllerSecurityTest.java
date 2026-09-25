package com.devopsai.backend.controller;

import com.devopsai.backend.dto.CodebaseIndexStatusDto;
import com.devopsai.backend.dto.RagQueryRequest;
import com.devopsai.backend.dto.RagQueryResponse;
import com.devopsai.backend.dto.RagSourceDto;
import com.devopsai.backend.security.ProjectSecurityEvaluator;
import com.devopsai.backend.service.RagCodebaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RagControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RagCodebaseService ragCodebaseService;

    @MockBean(name = "projectSecurity")
    private ProjectSecurityEvaluator projectSecurityEvaluator;

    @MockBean
    private com.devopsai.backend.repository.IssueRepository issueRepository;

    @Test
    void queryCodebase_Unauthenticated_ShouldReturn401Unauthorized() throws Exception {
        RagQueryRequest request = new RagQueryRequest(1L, "Where is auth?", 5);
        mockMvc.perform(post("/api/rag/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "dev_user", roles = {"DEVELOPER"})
    void queryCodebase_Authenticated_ShouldReturnRagResponse() throws Exception {
        when(projectSecurityEvaluator.hasProjectPermission(eq(1L), any(), eq("VIEWER"))).thenReturn(true);

        RagSourceDto source = new RagSourceDto("SecurityConfig.java", 72, 92, 0.95, "filterChain", "SOURCE_CODE");
        RagQueryResponse response = new RagQueryResponse(
            "Auth is in SecurityConfig.java",
            0.95,
            List.of(source),
            List.of("What security constraints apply?")
        );

        when(ragCodebaseService.queryCodebase(any(RagQueryRequest.class))).thenReturn(response);

        RagQueryRequest request = new RagQueryRequest(1L, "Where is auth?", 5);

        mockMvc.perform(post("/api/rag/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Auth is in SecurityConfig.java"))
                .andExpect(jsonPath("$.sources[0].filename").value("SecurityConfig.java"));
    }

    @Test
    @WithMockUser(username = "dev_user", roles = {"DEVELOPER"})
    void getIndexStatus_Authenticated_ShouldReturnStatus() throws Exception {
        when(projectSecurityEvaluator.hasProjectPermission(eq(1L), any(), eq("VIEWER"))).thenReturn(true);

        CodebaseIndexStatusDto statusDto = new CodebaseIndexStatusDto(1L, 10, 4, LocalDateTime.now(), false);
        when(ragCodebaseService.getIndexStatus(1L)).thenReturn(statusDto);

        mockMvc.perform(get("/api/rag/status/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.totalChunks").value(10));
    }
}
