import { NavLink } from 'react-router-dom';

// 상단 메뉴. TODO(F-08): 관리자 메뉴는 관리자에게만 표시 (권한 검사는 서버에서 다시 한다)
export default function NavBar() {
  return (
    <nav className="nav">
      <NavLink to="/documents">문서 목록</NavLink>
      <NavLink to="/qa">질문하기</NavLink>
      <NavLink to="/history">질의 이력</NavLink>
      <NavLink to="/admin/upload">업로드</NavLink>
      <NavLink to="/admin/tags">분류·태그 관리</NavLink>
    </nav>
  );
}
