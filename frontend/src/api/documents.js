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

export function updateSummary(id, summary, keywords) {
  return request(`/documents/${id}/summary`, {
    method: 'PUT',
    body: JSON.stringify({ summary, keywords }),
  });
}

export function deleteDocument(id) {
  return request(`/documents/${id}`, { method: 'DELETE' });
}
