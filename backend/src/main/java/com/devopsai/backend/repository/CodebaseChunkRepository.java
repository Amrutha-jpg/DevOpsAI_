package com.devopsai.backend.repository;

import com.devopsai.backend.entity.CodebaseChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodebaseChunkRepository extends JpaRepository<CodebaseChunk, Long> {
    List<CodebaseChunk> findByProjectId(Long projectId);
    long countByProjectId(Long projectId);
    void deleteByProjectId(Long projectId);
}
