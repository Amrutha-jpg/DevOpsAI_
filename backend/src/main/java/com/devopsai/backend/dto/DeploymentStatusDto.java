package com.devopsai.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class DeploymentStatusDto {

    private String environmentStatus; // PRODUCTION, DOCKER_CONTAINERIZED, STAGING, LOCAL_DEV
    private boolean dbConnected;
    private String dbStatus;
    private boolean redisHealthy;
    private String redisStatus;
    private long jvmMemoryUsedMb;
    private long jvmMemoryMaxMb;
    private long systemUptimeSeconds;
    private int activeContainersCount;
    private LocalDateTime latestDeploymentTime;
    private List<ContainerStatusDto> containers;

    public DeploymentStatusDto() {}

    public DeploymentStatusDto(String environmentStatus, boolean dbConnected, String dbStatus,
                               boolean redisHealthy, String redisStatus, long jvmMemoryUsedMb,
                               long jvmMemoryMaxMb, long systemUptimeSeconds, int activeContainersCount,
                               LocalDateTime latestDeploymentTime, List<ContainerStatusDto> containers) {
        this.environmentStatus = environmentStatus;
        this.dbConnected = dbConnected;
        this.dbStatus = dbStatus;
        this.redisHealthy = redisHealthy;
        this.redisStatus = redisStatus;
        this.jvmMemoryUsedMb = jvmMemoryUsedMb;
        this.jvmMemoryMaxMb = jvmMemoryMaxMb;
        this.systemUptimeSeconds = systemUptimeSeconds;
        this.activeContainersCount = activeContainersCount;
        this.latestDeploymentTime = latestDeploymentTime;
        this.containers = containers;
    }

    public String getEnvironmentStatus() {
        return environmentStatus;
    }

    public void setEnvironmentStatus(String environmentStatus) {
        this.environmentStatus = environmentStatus;
    }

    public boolean isDbConnected() {
        return dbConnected;
    }

    public void setDbConnected(boolean dbConnected) {
        this.dbConnected = dbConnected;
    }

    public String getDbStatus() {
        return dbStatus;
    }

    public void setDbStatus(String dbStatus) {
        this.dbStatus = dbStatus;
    }

    public boolean isRedisHealthy() {
        return redisHealthy;
    }

    public void setRedisHealthy(boolean redisHealthy) {
        this.redisHealthy = redisHealthy;
    }

    public String getRedisStatus() {
        return redisStatus;
    }

    public void setRedisStatus(String redisStatus) {
        this.redisStatus = redisStatus;
    }

    public long getJvmMemoryUsedMb() {
        return jvmMemoryUsedMb;
    }

    public void setJvmMemoryUsedMb(long jvmMemoryUsedMb) {
        this.jvmMemoryUsedMb = jvmMemoryUsedMb;
    }

    public long getJvmMemoryMaxMb() {
        return jvmMemoryMaxMb;
    }

    public void setJvmMemoryMaxMb(long jvmMemoryMaxMb) {
        this.jvmMemoryMaxMb = jvmMemoryMaxMb;
    }

    public long getSystemUptimeSeconds() {
        return systemUptimeSeconds;
    }

    public void setSystemUptimeSeconds(long systemUptimeSeconds) {
        this.systemUptimeSeconds = systemUptimeSeconds;
    }

    public int getActiveContainersCount() {
        return activeContainersCount;
    }

    public void setActiveContainersCount(int activeContainersCount) {
        this.activeContainersCount = activeContainersCount;
    }

    public LocalDateTime getLatestDeploymentTime() {
        return latestDeploymentTime;
    }

    public void setLatestDeploymentTime(LocalDateTime latestDeploymentTime) {
        this.latestDeploymentTime = latestDeploymentTime;
    }

    public List<ContainerStatusDto> getContainers() {
        return containers;
    }

    public void setContainers(List<ContainerStatusDto> containers) {
        this.containers = containers;
    }
}
