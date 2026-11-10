// 서버 API 호출 공통 함수. API 키는 프론트에 두지 않는다.
export async function request(path, options = {}) {
  const isFormData = options.body instanceof FormData;
  const headers = isFormData
    ? { ...options.headers }
    : { 'Content-Type': 'application/json', ...options.headers };
  const response = await fetch(`/api${path}`, { ...options, headers });
  if (!response.ok) {
    let message = `요청 실패 (${response.status})`;
    try {
      const data = await response.json();
      if (data.message) {
        message = data.message;
      }
    } catch {
      // JSON이 아닌 오류 응답은 기본 문구를 쓴다
    }
    throw new Error(message);
  }
  return response.status === 204 ? null : response.json();
}
