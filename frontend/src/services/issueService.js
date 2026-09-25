import apiClient from './apiClient';

export const issueService = {
  // Issues
  getIssues: async (projectId, params = {}) => {
    const response = await apiClient.get(`/projects/${projectId}/issues`, { params });
    return response.data;
  },

  getPrioritizedBacklog: async (projectId) => {
    const response = await apiClient.get(`/projects/${projectId}/issues/prioritized`);
    return response.data;
  },

  createIssue: async (projectId, issueData) => {
    const response = await apiClient.post(`/projects/${projectId}/issues`, issueData);
    return response.data;
  },

  updateIssueStatus: async (issueId, status) => {
    const response = await apiClient.put(`/issues/${issueId}/status`, null, {
      params: { status }
    });
    return response.data;
  },

  updateIssue: async (issueId, issueData) => {
    const response = await apiClient.put(`/issues/${issueId}`, issueData);
    return response.data;
  },

  deleteIssue: async (issueId) => {
    const response = await apiClient.delete(`/issues/${issueId}`);
    return response.data;
  },

  // Epics
  getEpics: async (projectId) => {
    const response = await apiClient.get(`/projects/${projectId}/epics`);
    return response.data;
  },

  createEpic: async (projectId, epicData) => {
    const response = await apiClient.post(`/projects/${projectId}/epics`, epicData);
    return response.data;
  },

  // Sprints
  getSprints: async (projectId) => {
    const response = await apiClient.get(`/projects/${projectId}/sprints`);
    return response.data;
  },

  createSprint: async (projectId, sprintData) => {
    const response = await apiClient.post(`/projects/${projectId}/sprints`, sprintData);
    return response.data;
  },

  updateSprintStatus: async (sprintId, status) => {
    const response = await apiClient.put(`/sprints/${sprintId}/status`, null, {
      params: { status }
    });
    return response.data;
  }
};
