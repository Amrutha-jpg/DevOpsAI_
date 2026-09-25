package com.devopsai.backend.repository;

import com.devopsai.backend.entity.TestPipelineRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestPipelineRunRepository extends JpaRepository<TestPipelineRun, Long> {
    List<TestPipelineRun> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    Optional<TestPipelineRun> findFirstByProjectIdOrderByCreatedAtDesc(Long projectId);
    void deleteByProjectId(Long projectId);
}
