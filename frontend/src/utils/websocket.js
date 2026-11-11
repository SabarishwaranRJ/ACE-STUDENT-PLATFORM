import { Client } from '@stomp/stompjs';

export const createStompClient = (onConnect, onError) => {
  const token = localStorage.getItem('token');
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  const brokerURL = `${protocol}//${window.location.host}/ws`;

  const client = new Client({
    brokerURL: brokerURL,
    connectHeaders: {
      Authorization: token ? `Bearer ${token}` : '',
      token: token || '',
    },
    debug: () => {},
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
  });

  client.onConnect = (frame) => {
    if (onConnect) onConnect(client, frame);
  };

  client.onStompError = (frame) => {
    console.warn('STOMP Error:', frame.headers?.['message'], frame.body);
    if (onError) onError(frame);
  };

  client.onWebSocketError = (event) => {
    console.warn('WebSocket connection error:', event);
  };

  return client;
};
