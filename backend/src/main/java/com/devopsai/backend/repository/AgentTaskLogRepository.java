package com.devopsai.backend.repository;

import com.devopsai.backend.entity.AgentTaskLog;
import com.devopsai.backend.entity.AgentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentTaskLogRepository extends JpaRepository<AgentTaskLog, Long> {
    List<AgentTaskLog> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    List<AgentTaskLog> findByProjectIdAndAgentTypeOrderByCreatedAtDesc(Long projectId, AgentType agentType);
    void deleteByProjectId(Long projectId);
}
