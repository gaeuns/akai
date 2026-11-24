import { Navigate, Route, Routes } from 'react-router-dom';
import NavBar from './components/NavBar.jsx';
import LoginPage from './pages/LoginPage.jsx';
import DocumentListPage from './pages/DocumentListPage.jsx';
import DocumentDetailPage from './pages/DocumentDetailPage.jsx';
import QaPage from './pages/QaPage.jsx';
import HistoryPage from './pages/HistoryPage.jsx';
import UploadPage from './pages/UploadPage.jsx';
import TagManagePage from './pages/TagManagePage.jsx';
import ComparePage from './pages/ComparePage.jsx';
import { useAuth } from './utils/auth.js';

// 화면 흐름도(시스템 설계서 9절)의 화면 8개. 근거 원문 보기는 QaPage 안의 팝업이다.
// 로그인 전에는 로그인 화면만, 관리자 화면은 관리자만 열 수 있다.
export default function App() {
  const { user, checking, isAdmin } = useAuth();

  if (checking) {
    return <p className="container">불러오는 중...</p>;
  }

  if (!user) {
    return (
      <main className="container login-container">
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </main>
    );
  }

  const adminOnly = (page) => (isAdmin ? page : <Navigate to="/documents" replace />);

  return (
    <>
      <NavBar />
      <main className="container">
        <Routes>
          <Route path="/documents" element={<DocumentListPage />} />
          <Route path="/documents/:id" element={<DocumentDetailPage />} />
          <Route path="/compare" element={<ComparePage />} />
          <Route path="/qa" element={<QaPage />} />
          <Route path="/history" element={<HistoryPage />} />
          <Route path="/admin/upload" element={adminOnly(<UploadPage />)} />
          <Route path="/admin/tags" element={adminOnly(<TagManagePage />)} />
          <Route path="*" element={<Navigate to="/documents" replace />} />
        </Routes>
      </main>
    </>
  );
}
