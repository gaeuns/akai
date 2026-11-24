import { NavLink } from 'react-router-dom';
import { useAuth } from '../utils/auth.js';

// 상단 메뉴. 관리자 메뉴는 관리자에게만 보인다 (권한 검사는 서버에서 다시 한다)
export default function NavBar() {
  const { user, isAdmin, logout } = useAuth();

  return (
    <nav className="nav">
      <NavLink to="/documents">문서 목록</NavLink>
      <NavLink to="/qa">질문하기</NavLink>
      <NavLink to="/history">질의 이력</NavLink>
      {isAdmin && (
        <>
          <NavLink to="/admin/upload">업로드</NavLink>
          <NavLink to="/admin/tags">분류·태그 관리</NavLink>
          <NavLink to="/admin/search-compare">검색 비교</NavLink>
          <NavLink to="/admin/dashboard">품질 지표</NavLink>
        </>
      )}
      <div className="nav-user">
        <span>
          {user.name} <span className="role">{isAdmin ? '관리자' : '이용자'}</span>
        </span>
        <button type="button" className="ghost" onClick={logout}>
          로그아웃
        </button>
      </div>
    </nav>
  );
}
