import apiClient from './client';

export const workspaceApi = {
  // Overview
  getWorkspace: (teamId) => apiClient.get(`/teams/${teamId}/workspace`),

  // Group Chat
  getChatMessages: (teamId) => apiClient.get(`/teams/${teamId}/messages`),
  sendMessage: (teamId, data) => apiClient.post(`/teams/${teamId}/messages`, data),
  deleteMessage: (teamId, messageId) => apiClient.delete(`/teams/${teamId}/messages/${messageId}`),

  // Tasks (Kanban)
  getTasks: (teamId) => apiClient.get(`/teams/${teamId}/tasks`),
  createTask: (teamId, data) => apiClient.post(`/teams/${teamId}/tasks`, data),
  updateTask: (teamId, taskId, data) => apiClient.put(`/teams/${teamId}/tasks/${taskId}`, data),
  updateTaskStatus: (teamId, taskId, status) => apiClient.patch(`/teams/${teamId}/tasks/${taskId}/status`, { status }),
  deleteTask: (teamId, taskId) => apiClient.delete(`/teams/${teamId}/tasks/${taskId}`),

  // Shared Files
  getFiles: (teamId, category) => apiClient.get(`/teams/${teamId}/files`, { params: category ? { category } : {} }),
  uploadFile: (teamId, formData) => apiClient.post(`/teams/${teamId}/files/upload`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  deleteFile: (teamId, fileId) => apiClient.delete(`/teams/${teamId}/files/${fileId}`),

  // Announcements
  getAnnouncements: (teamId) => apiClient.get(`/teams/${teamId}/announcements`),
  createAnnouncement: (teamId, data) => apiClient.post(`/teams/${teamId}/announcements`, data),
  updateAnnouncement: (teamId, id, data) => apiClient.put(`/teams/${teamId}/announcements/${id}`, data),
  togglePinAnnouncement: (teamId, id) => apiClient.patch(`/teams/${teamId}/announcements/${id}/pin`),
  deleteAnnouncement: (teamId, id) => apiClient.delete(`/teams/${teamId}/announcements/${id}`),

  // Meetings
  getMeetings: (teamId) => apiClient.get(`/teams/${teamId}/meetings`),
  scheduleMeeting: (teamId, data) => apiClient.post(`/teams/${teamId}/meetings`, data),
  cancelMeeting: (teamId, meetingId) => apiClient.delete(`/teams/${teamId}/meetings/${meetingId}`),

  // Direct Messages
  getConversations: () => apiClient.get('/conversations'),
  getOrCreateConversation: (targetStudentId) => apiClient.post(`/conversations/direct/${targetStudentId}`),
  getConversationMessages: (conversationId) => apiClient.get(`/conversations/${conversationId}/messages`),
  sendDirectMessage: (conversationId, data) => apiClient.post(`/conversations/${conversationId}/messages`, data),
  markConversationAsRead: (conversationId) => apiClient.patch(`/conversations/${conversationId}/read`),

  // Notifications
  getNotifications: (limit = 30) => apiClient.get(`/notifications?limit=${limit}`),
  getUnreadNotificationCount: () => apiClient.get('/notifications/unread-count'),
  markNotificationAsRead: (id) => apiClient.patch(`/notifications/${id}/read`),
  markAllNotificationsAsRead: () => apiClient.patch('/notifications/read-all'),
};

