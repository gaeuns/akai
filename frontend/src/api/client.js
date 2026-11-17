// 서버 API 호출 공통 함수. API 키는 프론트에 두지 않는다.

// 로그인이 풀린 상태(401)를 AuthProvider에 알린다. 로그인 실패(401)는 화면에서 직접 안내한다.
let handleUnauthorized = () => {};

export function setUnauthorizedHandler(handler) {
  handleUnauthorized = handler;
}

export async function request(path, options = {}) {
  const isFormData = options.body instanceof FormData;
  const headers = isFormData
    ? { ...options.headers }
    : { 'Content-Type': 'application/json', ...options.headers };
  let response;
  try {
    response = await fetch(`/api${path}`, { ...options, headers });
  } catch {
    throw new Error('서버에 연결할 수 없습니다. 서버가 꺼져 있거나 파일이 너무 클 수 있습니다.');
  }
  if (response.status === 401 && path !== '/auth/login') {
    handleUnauthorized();
  }
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
