import { request } from './client.js';

// F-04 · UC-05: 검색어(제목·키워드·태그)와 분류로 찾는다. 둘 다 비면 전체 목록
export function fetchDocuments(keyword = '', categoryId = '') {
  const params = new URLSearchParams();
  if (keyword) {
    params.set('keyword', keyword);
  }
  if (categoryId) {
    params.set('categoryId', categoryId);
  }
  const query = params.toString();
  return request(`/documents${query ? `?${query}` : ''}`);
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

// F-04 · UC-03: 문서의 분류·태그를 바꾼다 (관리자)
export function classifyDocument(id, categoryId, tagIds) {
  return request(`/documents/${id}/classification`, {
    method: 'PUT',
    body: JSON.stringify({ categoryId: categoryId || null, tagIds }),
  });
}
