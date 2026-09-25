package com.devopsai.backend.repository;

import com.devopsai.backend.entity.Epic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EpicRepository extends JpaRepository<Epic, Long> {
    List<Epic> findByProjectId(Long projectId);
    void deleteByProjectId(Long projectId);
}
