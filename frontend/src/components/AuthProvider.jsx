import { useEffect, useMemo, useState } from 'react';
import { fetchMe, login as requestLogin, logout as requestLogout } from '../api/auth.js';
import { setUnauthorizedHandler } from '../api/client.js';
import { AuthContext } from '../utils/auth.js';

// UC-01 · F-08: 로그인 상태를 앱 전체에 알려 준다. 새로고침해도 서버 세션으로 로그인이 유지된다.
export default function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [checking, setChecking] = useState(true);

  useEffect(() => {
    setUnauthorizedHandler(() => setUser(null));
    const checkSession = async () => {
      try {
        setUser(await fetchMe());
      } catch {
        setUser(null);
      } finally {
        setChecking(false);
      }
    };
    checkSession();
  }, []);

  const value = useMemo(
    () => ({
      user,
      checking,
      isAdmin: user?.role === 'ADMIN',
      login: async (loginId, password) => {
        setUser(await requestLogin(loginId, password));
      },
      logout: async () => {
        try {
          await requestLogout();
        } finally {
          setUser(null);
        }
      },
    }),
    [user, checking],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
