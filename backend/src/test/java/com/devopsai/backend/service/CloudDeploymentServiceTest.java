package com.devopsai.backend.service;

import com.devopsai.backend.dto.DeploymentStatusDto;
import com.devopsai.backend.dto.PipelineRunDto;
import com.devopsai.backend.dto.TriggerPipelineRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CloudDeploymentServiceTest {

    @InjectMocks
    private CloudDeploymentService cloudDeploymentService;

    @BeforeEach
    void setUp() {
        cloudDeploymentService = new CloudDeploymentService();
    }

    @Test
    void testGetDeploymentStatus_ReturnsValidInfrastructureHealth() {
        DeploymentStatusDto status = cloudDeploymentService.getDeploymentStatus();

        assertNotNull(status);
        assertNotNull(status.getEnvironmentStatus());
        assertTrue(status.isDbConnected());
        assertTrue(status.isRedisHealthy());
        assertTrue(status.getJvmMemoryUsedMb() >= 0);
        assertTrue(status.getSystemUptimeSeconds() >= 0);
        assertEquals(4, status.getContainers().size());
    }

    @Test
    void testInitiateDeploymentPipeline_AddsToHistoryAndReturnsPendingOrDeployedRun() {
        TriggerPipelineRequest request = new TriggerPipelineRequest("STAGING", "f1e2d3c4");
        PipelineRunDto run = cloudDeploymentService.initiateDeploymentPipeline(request, "admin");

        assertNotNull(run);
        assertNotNull(run.getId());
        assertEquals("STAGING", run.getTargetEnvironment());
        assertEquals("f1e2d3c4", run.getCommitSha());
        assertNotNull(run.getStatus());
        assertEquals("admin", run.getTriggeredBy());

        List<PipelineRunDto> history = cloudDeploymentService.getPipelineHistory();
        assertFalse(history.isEmpty());
        assertEquals(run.getId(), history.get(0).getId());
    }

    @Test
    void testTriggerPipelineAsync_UpdatesStatusToDeployed() throws Exception {
        TriggerPipelineRequest request = new TriggerPipelineRequest("PRODUCTION", "abc1234");
        PipelineRunDto run = cloudDeploymentService.initiateDeploymentPipeline(request, "devuser");

        // Execute async logic synchronously for test verification
        cloudDeploymentService.triggerPipelineAsync(run.getId());

        List<PipelineRunDto> history = cloudDeploymentService.getPipelineHistory();
        PipelineRunDto updated = history.stream()
                .filter(p -> p.getId().equals(run.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(updated);
        assertEquals("DEPLOYED", updated.getStatus());
        assertNotNull(updated.getCompletedAt());
        assertEquals(45L, updated.getDurationSeconds());
    }
}
