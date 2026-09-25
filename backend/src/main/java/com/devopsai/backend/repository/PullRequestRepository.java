package com.devopsai.backend.repository;

import com.devopsai.backend.entity.PullRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PullRequestRepository extends JpaRepository<PullRequest, Long> {
    List<PullRequest> findByGithubRepositoryIdOrderByNumberDesc(Long githubRepositoryId);
    Optional<PullRequest> findByGithubRepositoryIdAndNumber(Long githubRepositoryId, int number);
    List<PullRequest> findByGithubRepositoryId(Long githubRepositoryId);
    void deleteByGithubRepositoryId(Long githubRepositoryId);
}
