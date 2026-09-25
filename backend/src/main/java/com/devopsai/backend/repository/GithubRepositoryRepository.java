package com.devopsai.backend.repository;

import com.devopsai.backend.entity.GithubRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GithubRepositoryRepository extends JpaRepository<GithubRepository, Long> {
    Optional<GithubRepository> findByProjectId(Long projectId);
    Optional<GithubRepository> findByOwnerNameAndRepoName(String ownerName, String repoName);
    void deleteByProjectId(Long projectId);
}
