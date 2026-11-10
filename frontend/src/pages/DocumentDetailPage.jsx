import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import AiBadge from '../components/AiBadge.jsx';
import { deleteDocument, fetchDocument } from '../api/documents.js';
import { formatDateTime } from '../utils/format.js';

// UC-06 · F-02: 요약·키워드(AI 생성 표시)와 조각 목록.
// TODO(F-02): 관리자 요약 수정, TODO(F-04): 태그 표시
export default function DocumentDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [document, setDocument] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchDocument(id)
      .then(setDocument)
      .catch((e) => setError(e.message));
  }, [id]);

  const handleDelete = async () => {
    if (!window.confirm('이 문서를 삭제할까요? 조각과 요약도 함께 삭제됩니다.')) {
      return;
    }
    try {
      await deleteDocument(id);
      navigate('/documents');
    } catch (e) {
      setError(e.message);
    }
  };

  if (error) {
    return <p className="error">{error}</p>;
  }
  if (!document) {
    return <p>불러오는 중...</p>;
  }

  return (
    <section>
      <h1>{document.title}</h1>
      <p className="muted">
        {document.fileName} · {document.fileType} · 조각 {document.chunks.length}개 · 등록{' '}
        {formatDateTime(document.createdAt)}
      </p>

      <h2>요약 {document.aiGenerated && <AiBadge />}</h2>
      {document.summary ? (
        <p className="summary">{document.summary}</p>
      ) : (
        <p className="muted">요약이 없습니다. (OpenAI 키가 없거나 요약 생성에 실패)</p>
      )}

      <h2>키워드</h2>
      <p>{document.keywords || <span className="muted">없음</span>}</p>

      <h2>조각 ({document.chunks.length})</h2>
      <ol className="chunks">
        {document.chunks.map((chunk) => (
          <li key={chunk.seq}>
            <div className="muted">
              {chunk.pageNo}쪽 · #{chunk.seq}
            </div>
            <p className="chunk-content">{chunk.content}</p>
          </li>
        ))}
      </ol>

      <button type="button" className="danger" onClick={handleDelete}>
        문서 삭제
      </button>
    </section>
  );
}
