import apiClient from './apiClient';

export const aiReviewService = {
  triggerPrReview: async (projectId, pullRequestId) => {
    const response = await apiClient.post(`/projects/${projectId}/reviews/analyze-pr/${pullRequestId}`);
    return response.data;
  },

  analyzeSnippet: async (filename, codeSnippet, projectId) => {
    const response = await apiClient.post('/reviews/analyze-snippet', {
      filename,
      codeSnippet,
      projectId
    });
    return response.data;
  },

  getReview: async (reviewId) => {
    const response = await apiClient.get(`/reviews/${reviewId}`);
    return response.data;
  },

  getReviewsForProject: async (projectId) => {
    const response = await apiClient.get(`/projects/${projectId}/reviews`);
    return response.data;
  }
};

export default aiReviewService;
