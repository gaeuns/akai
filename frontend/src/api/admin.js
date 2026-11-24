import { request } from './client.js';

// 관리자 전용 API (서버가 /api/admin/** 권한을 다시 검사한다)
export function compareSearch(question) {
  return request('/admin/search-comparison', {
    method: 'POST',
    body: JSON.stringify({ question }),
  });
}

export function fetchStatistics() {
  return request('/admin/statistics');
}
