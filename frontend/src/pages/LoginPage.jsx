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
      <div className="login-brand">
        <div className="login-logo">AKAI</div>
        <div>
          <p className="login-lead">기관 문서에 적힌 내용만으로 답합니다.</p>
          <p className="login-sub">
            문서를 올리면 요약과 키워드를 만들고, 질문에는 근거 원문과 함께 답합니다.
          </p>
        </div>
      </div>
      <div className="login-panel">
        <h1>로그인</h1>
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
        <p className="muted small">
          계정은 기관에서 발급합니다. 계정이 없으면 관리자에게 문의하세요.
        </p>
      </div>
    </section>
  );
}
