import { Navigate, Route, Routes } from 'react-router-dom';
import NavBar from './components/NavBar.jsx';
import LoginPage from './pages/LoginPage.jsx';
import DocumentListPage from './pages/DocumentListPage.jsx';
import DocumentDetailPage from './pages/DocumentDetailPage.jsx';
import QaPage from './pages/QaPage.jsx';
import HistoryPage from './pages/HistoryPage.jsx';
import UploadPage from './pages/UploadPage.jsx';
import TagManagePage from './pages/TagManagePage.jsx';

// 화면 흐름도(시스템 설계서 9절)의 화면 8개. 근거 원문 보기는 QaPage 안의 팝업이다.
export default function App() {
  return (
    <>
      <NavBar />
      <main className="container">
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/documents" element={<DocumentListPage />} />
          <Route path="/documents/:id" element={<DocumentDetailPage />} />
          <Route path="/qa" element={<QaPage />} />
          <Route path="/history" element={<HistoryPage />} />
          <Route path="/admin/upload" element={<UploadPage />} />
          <Route path="/admin/tags" element={<TagManagePage />} />
          <Route path="*" element={<Navigate to="/documents" replace />} />
        </Routes>
      </main>
    </>
  );
}
