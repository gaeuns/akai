import { request } from './client.js';

export function fetchHistory() {
  return request('/questions');
}

export function askQuestion(question) {
  return request('/questions', {
    method: 'POST',
    body: JSON.stringify({ question }),
  });
}
