import apiClient from './apiClient';

export const executeAgentTask = async (projectId, agentType, targetId = null, inputPayload = '') => {
  const response = await apiClient.post('/agents/execute', {
    projectId,
    agentType,
    targetId,
    inputPayload,
  });
  return response.data;
};

export const getTaskLogsForProject = async (projectId) => {
  const response = await apiClient.get(`/agents/tasks/${projectId}`);
  return response.data;
};

export const getTaskDetail = async (taskId) => {
  const response = await apiClient.get(`/agents/tasks/detail/${taskId}`);
  return response.data;
};

export const approveTask = async (taskId, feedback = '') => {
  const response = await apiClient.post(`/agents/tasks/${taskId}/approve`, { taskId, decision: 'APPROVED', feedback });
  return response.data;
};

export const rejectTask = async (taskId, feedback = '') => {
  const response = await apiClient.post(`/agents/tasks/${taskId}/reject`, { taskId, decision: 'REJECTED', feedback });
  return response.data;
};
