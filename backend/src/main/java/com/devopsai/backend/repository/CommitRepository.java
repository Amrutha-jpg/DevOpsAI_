package com.devopsai.backend.repository;

import com.devopsai.backend.entity.Commit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommitRepository extends JpaRepository<Commit, Long> {
    List<Commit> findByGithubRepositoryIdOrderByCommitDateDesc(Long githubRepositoryId);
    Optional<Commit> findByGithubRepositoryIdAndSha(Long githubRepositoryId, String sha);
    void deleteByGithubRepositoryId(Long githubRepositoryId);
}
