package com.devopsai.backend.repository;

import com.devopsai.backend.entity.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SprintRepository extends JpaRepository<Sprint, Long> {
    List<Sprint> findByProjectId(Long projectId);
    void deleteByProjectId(Long projectId);
}
