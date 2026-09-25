import apiClient from './apiClient';

export const queryCodebase = async (projectId, query) => {
  const response = await apiClient.post('/rag/query', { projectId, query, maxSources: 5 });
  return response.data;
};

export const reindexCodebase = async (projectId) => {
  const response = await apiClient.post(`/rag/index/${projectId}`);
  return response.data;
};

export const getIndexStatus = async (projectId) => {
  const response = await apiClient.get(`/rag/status/${projectId}`);
  return response.data;
};

export const getSuggestedQuestions = async (projectId) => {
  const response = await apiClient.get(`/rag/suggested-questions/${projectId}`);
  return response.data;
};
