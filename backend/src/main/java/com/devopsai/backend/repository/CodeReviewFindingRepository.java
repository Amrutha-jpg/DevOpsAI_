package com.devopsai.backend.repository;

import com.devopsai.backend.entity.CodeReviewFinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeReviewFindingRepository extends JpaRepository<CodeReviewFinding, Long> {
    List<CodeReviewFinding> findByCodeReviewId(Long codeReviewId);
}
