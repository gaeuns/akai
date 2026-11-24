import { request } from './client.js';

export function compareDocuments(documentIds) {
  return request('/comparisons', {
    method: 'POST',
    body: JSON.stringify({ documentIds }),
  });
}
