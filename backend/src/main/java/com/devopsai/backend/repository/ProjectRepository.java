package com.devopsai.backend.repository;

import com.devopsai.backend.entity.Project;
import com.devopsai.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByProjectKey(String projectKey);

    Boolean existsByProjectKey(String projectKey);

    List<Project> findByOwner(User owner);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN ProjectMember pm ON p.id = pm.project.id WHERE p.owner = :user OR pm.user = :user")
    List<Project> findAllAccessibleToUser(@Param("user") User user);
}
