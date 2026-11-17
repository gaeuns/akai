import { createContext, useContext } from 'react';

// 로그인 상태: { user, checking, isAdmin, login, logout }. 값은 AuthProvider가 채운다.
export const AuthContext = createContext(null);

export function useAuth() {
  return useContext(AuthContext);
}
