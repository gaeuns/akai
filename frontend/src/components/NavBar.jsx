import { NavLink } from 'react-router-dom';
import { useAuth } from '../utils/auth.js';

// 왼쪽 메뉴. 관리 메뉴는 관리자에게만 보인다 (권한 검사는 서버에서 다시 한다)
export default function NavBar() {
  const { user, isAdmin, logout } = useAuth();

  return (
    <nav className="nav" aria-label="주 메뉴">
      <div className="nav-logo">AKAI</div>
      <div className="nav-group">
        <NavLink to="/documents">문서</NavLink>
        <NavLink to="/qa">질문하기</NavLink>
        <NavLink to="/history">질의 이력</NavLink>
      </div>
      {isAdmin && (
        <div className="nav-group">
          <span className="nav-group-label">관리</span>
          <NavLink to="/admin/tags">분류와 태그</NavLink>
          <NavLink to="/admin/search-compare">검색 비교</NavLink>
          <NavLink to="/admin/dashboard">품질 지표</NavLink>
        </div>
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
