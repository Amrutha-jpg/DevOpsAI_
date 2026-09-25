import apiClient from './apiClient';

export const runSecurityScan = async (projectId, commitSha = '', simulateVulnerabilities = false) => {
  const response = await apiClient.post(`/security/projects/${projectId}/scan`, {
    projectId,
    commitSha,
    simulateVulnerabilities
  });
  return response.data;
};

export const getReportsForProject = async (projectId) => {
  const response = await apiClient.get(`/security/projects/${projectId}/reports`);
  return response.data;
};

export const getLatestReportForProject = async (projectId) => {
  const response = await apiClient.get(`/security/projects/${projectId}/latest`);
  return response.data;
};

export const getReportById = async (reportId) => {
  const response = await apiClient.get(`/security/reports/${reportId}`);
  return response.data;
};
