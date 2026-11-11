import apiClient from './client';

export const invitationsApi = {
  sendInvitation: (data) => apiClient.post('/invitations', data),
  getMyInvitations: () => apiClient.get('/invitations/inbox'),
  getSentInvitations: () => apiClient.get('/invitations/sent'),
  respondInvitation: (id, accept) => apiClient.put(`/invitations/${id}/respond`, { accept }),
  withdrawInvitation: (id) => apiClient.put(`/invitations/${id}/withdraw`),
};
