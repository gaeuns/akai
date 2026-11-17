import { request } from './client.js';

export function login(loginId, password) {
  return request('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ loginId, password }),
  });
}

export function logout() {
  return request('/auth/logout', { method: 'POST' });
}

export function fetchMe() {
  return request('/auth/me');
}
