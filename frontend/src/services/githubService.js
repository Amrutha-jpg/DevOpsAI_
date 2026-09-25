import apiClient from './apiClient';

export const githubService = {
  connectRepo: async (projectId, repoData) => {
    const response = await apiClient.post(`/projects/${projectId}/github/connect`, repoData);
    return response.data;
  },

  getRepo: async (projectId) => {
    try {
      const response = await apiClient.get(`/projects/${projectId}/github/repo`);
      return response.data;
    } catch (error) {
      if (error.response && error.response.status === 404) {
        return null;
      }
      throw error;
    }
  },

  triggerSync: async (projectId) => {
    const response = await apiClient.post(`/projects/${projectId}/github/sync`);
    return response.data;
  },

  getCommits: async (projectId) => {
    const response = await apiClient.get(`/projects/${projectId}/github/commits`);
    return response.data;
  },

  getPullRequests: async (projectId) => {
    const response = await apiClient.get(`/projects/${projectId}/github/pulls`);
    return response.data;
  },

  getChangedFiles: async (pullRequestId) => {
    const response = await apiClient.get(`/pulls/${pullRequestId}/files`);
    return response.data;
  }
};

export default githubService;
