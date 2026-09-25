package com.devopsai.backend.repository;

import com.devopsai.backend.entity.SecurityScanReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityScanReportRepository extends JpaRepository<SecurityScanReport, Long> {
    List<SecurityScanReport> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    Optional<SecurityScanReport> findFirstByProjectIdOrderByCreatedAtDesc(Long projectId);
    void deleteByProjectId(Long projectId);
}
