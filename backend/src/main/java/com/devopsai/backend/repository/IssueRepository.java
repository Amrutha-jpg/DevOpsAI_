package com.devopsai.backend.repository;

import com.devopsai.backend.entity.Issue;
import com.devopsai.backend.entity.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {
    List<Issue> findByProjectId(Long projectId);
    List<Issue> findByProjectIdAndSprintId(Long projectId, Long sprintId);
    List<Issue> findByProjectIdAndEpicId(Long projectId, Long epicId);
    List<Issue> findByProjectIdAndStatus(Long projectId, IssueStatus status);
    void deleteByProjectId(Long projectId);
}
