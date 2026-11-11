import apiClient from './client';

export const eventsApi = {
  getAllEvents: () => apiClient.get('/events'),
  getOpenEvents: () => apiClient.get('/events/open'),
  getEvent: (id) => apiClient.get(`/events/${id}`),
  createEvent: (data) => apiClient.post('/events', data),
  updateEvent: (id, data) => apiClient.put(`/events/${id}`, data),
  deleteEvent: (id) => apiClient.delete(`/events/${id}`),
};
