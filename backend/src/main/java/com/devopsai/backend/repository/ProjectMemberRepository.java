package com.devopsai.backend.repository;

import com.devopsai.backend.entity.Project;
import com.devopsai.backend.entity.ProjectMember;
import com.devopsai.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    List<ProjectMember> findByProject(Project project);

    List<ProjectMember> findByUser(User user);

    Optional<ProjectMember> findByProjectAndUser(Project project, User user);

    Boolean existsByProjectAndUser(Project project, User user);

    void deleteByProjectId(Long projectId);
}
