package com.devopsai.backend.repository;

import com.devopsai.backend.entity.CodeReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodeReviewRepository extends JpaRepository<CodeReview, Long> {
    List<CodeReview> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    Optional<CodeReview> findByPullRequestId(Long pullRequestId);
    void deleteByProjectId(Long projectId);
}
