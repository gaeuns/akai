import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import AiBadge from '../components/AiBadge.jsx';
import { fetchDocuments } from '../api/documents.js';
import { formatDateTime } from '../utils/format.js';

// UC-05 · F-04: 제목·키워드 검색. TODO(F-04): 태그 검색은 태그 기능 구현 후 추가
export default function DocumentListPage() {
  const [keyword, setKeyword] = useState('');
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const load = useCallback(async (searchKeyword) => {
    setLoading(true);
    setError('');
    try {
      setDocuments(await fetchDocuments(searchKeyword));
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load('');
  }, [load]);

  const handleSubmit = (event) => {
    event.preventDefault();
    load(keyword.trim());
  };

  return (
    <section>
      <h1>문서 목록·검색</h1>
      <form className="search" onSubmit={handleSubmit}>
        <input
          value={keyword}
          onChange={(event) => setKeyword(event.target.value)}
          placeholder="제목 또는 키워드로 검색"
        />
        <button type="submit">검색</button>
      </form>

      {error && <p className="error">{error}</p>}
      {loading && <p>불러오는 중...</p>}
      {!loading && !error && documents.length === 0 && (
        <p className="muted">
          문서가 없습니다. <Link to="/admin/upload">문서 업로드</Link>에서 올려 보세요.
        </p>
      )}

      {documents.length > 0 && (
        <table className="table">
          <thead>
            <tr>
              <th>제목</th>
              <th>형식</th>
              <th>키워드</th>
              <th>조각</th>
              <th>등록일</th>
            </tr>
          </thead>
          <tbody>
            {documents.map((document) => (
              <tr key={document.id}>
                <td>
                  <Link to={`/documents/${document.id}`}>{document.title}</Link>
                </td>
                <td>{document.fileType}</td>
                <td>
                  {document.keywords ? (
                    <>
                      {document.keywords} {document.aiGenerated && <AiBadge />}
                    </>
                  ) : (
                    <span className="muted">없음</span>
                  )}
                </td>
                <td>{document.chunkCount}</td>
                <td>{formatDateTime(document.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
