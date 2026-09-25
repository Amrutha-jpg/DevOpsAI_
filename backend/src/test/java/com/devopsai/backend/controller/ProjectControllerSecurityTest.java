package com.devopsai.backend.controller;

import com.devopsai.backend.dto.ProjectDto;
import com.devopsai.backend.dto.ProjectRequest;
import com.devopsai.backend.dto.UserDto;
import com.devopsai.backend.entity.Role;
import com.devopsai.backend.security.ProjectSecurityEvaluator;
import com.devopsai.backend.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProjectControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private com.devopsai.backend.repository.IssueRepository issueRepository;

    @MockBean(name = "projectSecurity")
    private ProjectSecurityEvaluator projectSecurityEvaluator;

    private ProjectDto sampleProject;
    private ProjectRequest sampleRequest;

    @BeforeEach
    void setUp() {
        UserDto owner = new UserDto(2L, "pm_user", "pm@devopsai.io", Role.PROJECT_MANAGER);
        sampleProject = new ProjectDto(1L, "SmartHealth", "Health app", "SMART", owner, LocalDateTime.now(), LocalDateTime.now());
        sampleRequest = new ProjectRequest("SmartHealth", "Health app", "SMART");
    }

    @Test
    void getProjects_Unauthenticated_ShouldReturn401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @WithMockUser(username = "dev_user", roles = {"DEVELOPER"})
    void getProjects_Authenticated_ShouldReturnProjectList() throws Exception {
        when(projectService.getProjectsForUser(any())).thenReturn(List.of(sampleProject));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("SmartHealth"))
                .andExpect(jsonPath("$[0].projectKey").value("SMART"));
    }

    @Test
    @WithMockUser(username = "admin_user", roles = {"ADMIN"})
    void createProject_AsAdmin_ShouldReturn201Created() throws Exception {
        when(projectService.createProject(any(ProjectRequest.class), any())).thenReturn(sampleProject);

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("SmartHealth"));
    }

    @Test
    @WithMockUser(username = "pm_user", roles = {"PROJECT_MANAGER"})
    void createProject_AsProjectManager_ShouldReturn201Created() throws Exception {
        when(projectService.createProject(any(ProjectRequest.class), any())).thenReturn(sampleProject);

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "viewer_user", roles = {"VIEWER"})
    void createProject_AsViewer_ShouldReturn403Forbidden() throws Exception {
        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @WithMockUser(username = "viewer_user", roles = {"VIEWER"})
    void updateProject_WithoutPermission_ShouldReturn403Forbidden() throws Exception {
        when(projectSecurityEvaluator.hasProjectPermission(eq(1L), any(), eq("PROJECT_MANAGER"))).thenReturn(false);

        mockMvc.perform(put("/api/projects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin_user", roles = {"ADMIN"})
    void updateProject_AsAdmin_ShouldReturnUpdatedProject() throws Exception {
        when(projectService.updateProject(eq(1L), any(ProjectRequest.class))).thenReturn(sampleProject);

        mockMvc.perform(put("/api/projects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("SmartHealth"));
    }

    @Test
    void deleteProject_Unauthenticated_ShouldReturn401Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(username = "pm_user", roles = {"PROJECT_MANAGER"})
    void deleteProject_AsProjectManager_ShouldReturn403Forbidden() throws Exception {
        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "dev_user", roles = {"DEVELOPER"})
    void deleteProject_AsDeveloper_ShouldReturn403Forbidden() throws Exception {
        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "admin_user", roles = {"ADMIN"})
    void deleteProject_AsAdmin_ShouldReturn204NoContent() throws Exception {
        doNothing().when(projectService).deleteProject(1L);

        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isNoContent());

        verify(projectService).deleteProject(1L);
    }

    @Test
    @WithMockUser(username = "admin_user", roles = {"ADMIN"})
    void deleteProject_NotFound_ShouldReturn404NotFound() throws Exception {
        doThrow(new com.devopsai.backend.exception.ResourceNotFoundException("Project not found with id: 999"))
                .when(projectService).deleteProject(999L);

        mockMvc.perform(delete("/api/projects/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
