import apiClient from './apiClient';

export const triggerPipeline = async (projectId, branch = 'main', commitSha = '') => {
  const response = await apiClient.post(`/testing/projects/${projectId}/trigger`, {
    projectId,
    branch,
    commitSha
  });
  return response.data;
};

export const getPipelineRunsForProject = async (projectId) => {
  const response = await apiClient.get(`/testing/projects/${projectId}/runs`);
  return response.data;
};

export const getPipelineRun = async (runId) => {
  const response = await apiClient.get(`/testing/runs/${runId}`);
  return response.data;
};

export const getGitHubWorkflowYaml = async () => {
  const response = await apiClient.get('/testing/github-workflow');
  return response.data;
};
