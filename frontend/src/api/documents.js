import { request } from './client.js';

export function fetchDocuments(keyword = '') {
  const query = keyword ? `?keyword=${encodeURIComponent(keyword)}` : '';
  return request(`/documents${query}`);
}

export function fetchDocument(id) {
  return request(`/documents/${id}`);
}

export function uploadDocument(file, title) {
  const form = new FormData();
  form.append('file', file);
  if (title) {
    form.append('title', title);
  }
  return request('/documents', { method: 'POST', body: form });
}

export function deleteDocument(id) {
  return request(`/documents/${id}`, { method: 'DELETE' });
}
