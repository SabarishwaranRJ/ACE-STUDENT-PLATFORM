import apiClient from './client';

export const teamsApi = {
  createTeam: (data) => apiClient.post('/teams', data),
  getTeam: (id) => apiClient.get(`/teams/${id}`),
  getTeamsForEvent: (eventId, status) => apiClient.get(`/teams/event/${eventId}`, { params: { status } }),
  getMyTeams: () => apiClient.get('/teams/my'),
  updateTeam: (id, data) => apiClient.put(`/teams/${id}`, data),
  requestToJoin: (data) => apiClient.post('/teams/join', data),
  approveMember: (teamId, studentId) => apiClient.post(`/teams/${teamId}/members/${studentId}/approve`),
  removeMember: (teamId, studentId) => apiClient.delete(`/teams/${teamId}/members/${studentId}`),
  leaveTeam: (teamId) => apiClient.post(`/teams/${teamId}/leave`),

  // Requirements
  addRequirement: (teamId, data) => apiClient.post(`/teams/${teamId}/requirements`, data),
  removeRequirement: (teamId, reqId) => apiClient.delete(`/teams/${teamId}/requirements/${reqId}`),
  getRequirements: (teamId) => apiClient.get(`/teams/${teamId}/requirements`),
};
