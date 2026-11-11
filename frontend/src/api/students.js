import apiClient from './client';

export const studentsApi = {
  getMyProfile: () => apiClient.get('/students/me'),
  updateMyProfile: (data) => apiClient.put('/students/me', data),
  getProfile: (id) => apiClient.get(`/students/${id}`),
  getAllStudents: () => apiClient.get('/students'),

  // Skills
  getMySkills: () => apiClient.get('/students/me/skills'),
  addOrUpdateSkill: (data) => apiClient.post('/students/me/skills', data),
  addSkill: (data) => apiClient.post('/students/me/skills', data),
  getAllSkills: () => apiClient.get('/skills'),
  removeSkill: (skillId) => apiClient.delete(`/students/me/skills/${skillId}`),

  // Interests
  getMyInterests: () => apiClient.get('/students/me/interests'),
  addInterest: (interestId) => apiClient.post(`/students/me/interests/${interestId}`),
  removeInterest: (interestId) => apiClient.delete(`/students/me/interests/${interestId}`),

  // Availability
  getMyAvailability: () => apiClient.get('/students/me/availability'),
  addAvailability: (data) => apiClient.post('/students/me/availability', data),
  removeAvailability: (id) => apiClient.delete(`/students/me/availability/${id}`),
};
