package com.devopsai.backend.security;

import com.devopsai.backend.entity.Project;
import com.devopsai.backend.entity.ProjectMember;
import com.devopsai.backend.entity.ProjectRole;
import com.devopsai.backend.entity.Role;
import com.devopsai.backend.entity.User;
import com.devopsai.backend.repository.IssueRepository;
import com.devopsai.backend.repository.ProjectMemberRepository;
import com.devopsai.backend.repository.ProjectRepository;
import com.devopsai.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("projectSecurity")
public class ProjectSecurityEvaluator {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    private final IssueRepository issueRepository;

    public ProjectSecurityEvaluator(ProjectRepository projectRepository,
                                    ProjectMemberRepository projectMemberRepository,
                                    UserRepository userRepository,
                                    IssueRepository issueRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.issueRepository = issueRepository;
    }

    public boolean hasProjectPermission(Long projectId, Authentication authentication, String requiredRole) {
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return false;
        }

        // Global ADMIN always has full permission
        if (principal.getRole() == Role.ADMIN) {
            return true;
        }

        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return false;
        }

        Project project = projectOpt.get();
        // Project Owner always has permission
        if (project.getOwner() != null && project.getOwner().getId().equals(principal.getId())) {
            return true;
        }

        Optional<User> userOpt = userRepository.findById(principal.getId());
        if (userOpt.isEmpty()) {
            return false;
        }

        Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectAndUser(project, userOpt.get());
        if (memberOpt.isEmpty()) {
            return false;
        }

        ProjectRole memberRole = memberOpt.get().getProjectRole();
        if ("PROJECT_MANAGER".equalsIgnoreCase(requiredRole)) {
            return memberRole == ProjectRole.PROJECT_MANAGER;
        } else if ("DEVELOPER".equalsIgnoreCase(requiredRole)) {
            return memberRole == ProjectRole.PROJECT_MANAGER || memberRole == ProjectRole.DEVELOPER;
        } else if ("VIEWER".equalsIgnoreCase(requiredRole)) {
            return true;
        }

        return false;
    }

    public boolean hasIssueProjectPermission(Long issueId, Authentication authentication, String requiredRole) {
        return issueRepository.findById(issueId)
            .map(issue -> hasProjectPermission(issue.getProject().getId(), authentication, requiredRole))
            .orElse(false);
    }
}
