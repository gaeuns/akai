import { useState } from 'react';
import { useAuth } from '../utils/auth.js';

// UC-01 · F-08: 미리 등록된 계정으로 로그인. 회원가입은 없다.
export default function LoginPage() {
  const { login } = useAuth();
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!loginId.trim() || !password) {
      setError('아이디와 비밀번호를 입력하세요.');
      return;
    }
    setSubmitting(true);
    setError('');
    try {
      await login(loginId.trim(), password);
    } catch (e) {
      setError(e.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section className="login">
      <div className="login-logo">AKAI</div>
      <p className="muted">기관 문서 요약·근거 기반 Q&amp;A</p>
      <form className="form" onSubmit={handleSubmit}>
        <label>
          아이디
          <input
            value={loginId}
            onChange={(event) => setLoginId(event.target.value)}
            autoComplete="username"
            autoFocus
          />
        </label>
        <label>
          비밀번호
          <input
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            autoComplete="current-password"
          />
        </label>
        <button type="submit" disabled={submitting}>
          {submitting ? '로그인 중...' : '로그인'}
        </button>
      </form>
      {error && <p className="error">{error}</p>}
      <p className="muted small">계정은 관리자가 미리 등록합니다.</p>
    </section>
  );
}
