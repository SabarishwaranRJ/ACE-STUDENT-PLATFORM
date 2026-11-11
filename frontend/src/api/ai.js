import apiClient from './client';

export const aiApi = {
  getRecommendations: (eventId, topK = 10) => apiClient.get(`/recommendations/event/${eventId}`, { params: { topK } }),
  getStoredRecommendations: (eventId) => apiClient.get(`/recommendations/event/${eventId}/stored`),
  sendFeedback: (data) => apiClient.post('/recommendations/feedback', data),
  checkHealth: () => apiClient.get('/recommendations/health'),

  // Optimization & Gap Analysis
  optimizeTeam: (data) => apiClient.post('/optimize/team', data),
  getSkillGap: (data) => apiClient.post('/optimize/skill-gap', data),
};
