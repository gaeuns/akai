import { request } from './client.js';

// F-04 · UC-03: 분류·태그. 조회는 모두, 추가·수정·삭제는 관리자만 (서버에서 검사)

export function fetchCategories() {
  return request('/categories');
}

export function createCategory(name) {
  return request('/categories', { method: 'POST', body: JSON.stringify({ name }) });
}

export function renameCategory(id, name) {
  return request(`/categories/${id}`, { method: 'PUT', body: JSON.stringify({ name }) });
}

export function deleteCategory(id) {
  return request(`/categories/${id}`, { method: 'DELETE' });
}

export function fetchTags() {
  return request('/tags');
}

export function createTag(name) {
  return request('/tags', { method: 'POST', body: JSON.stringify({ name }) });
}

export function renameTag(id, name) {
  return request(`/tags/${id}`, { method: 'PUT', body: JSON.stringify({ name }) });
}

export function deleteTag(id) {
  return request(`/tags/${id}`, { method: 'DELETE' });
}
