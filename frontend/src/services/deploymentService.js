import apiClient from './apiClient';

export const getDeploymentStatus = async () => {
  const response = await apiClient.get('/deployment/status');
  return response.data;
};

export const triggerDeploymentPipeline = async (targetEnvironment = 'PRODUCTION', commitSha = '') => {
  const response = await apiClient.post('/deployment/pipeline/trigger', {
    targetEnvironment,
    commitSha,
  });
  return response.data;
};

export const getPipelineHistory = async () => {
  const response = await apiClient.get('/deployment/pipeline/history');
  return response.data;
};
