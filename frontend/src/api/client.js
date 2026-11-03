// 서버 API 호출 공통 함수. API 키는 프론트에 두지 않는다.
export async function request(path, options = {}) {
  const response = await fetch(`/api${path}`, {
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options,
  });
  if (!response.ok) {
    throw new Error(`요청 실패: ${response.status}`);
  }
  return response.status === 204 ? null : response.json();
}
