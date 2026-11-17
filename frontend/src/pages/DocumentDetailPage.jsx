import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import AiBadge from '../components/AiBadge.jsx';
import { deleteDocument, fetchDocument, updateSummary } from '../api/documents.js';
import { useAuth } from '../utils/auth.js';
import { formatDateTime } from '../utils/format.js';

// UC-06 · F-02: 요약·키워드(AI 생성 표시)와 조각 목록.
// 관리자는 요약·키워드를 고칠 수 있다(F-02, AI 재호출 없음). TODO(F-04): 태그 표시
export default function DocumentDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAdmin } = useAuth();
  const [document, setDocument] = useState(null);
  const [error, setError] = useState('');
  const [editing, setEditing] = useState(false);
  const [summaryDraft, setSummaryDraft] = useState('');
  const [keywordsDraft, setKeywordsDraft] = useState('');
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState('');

  useEffect(() => {
    fetchDocument(id)
      .then(setDocument)
      .catch((e) => setError(e.message));
  }, [id]);

  const startEditing = () => {
    setSummaryDraft(document.summary ?? '');
    setKeywordsDraft(document.keywords ?? '');
    setSaveError('');
    setEditing(true);
  };

  const handleSave = async (event) => {
    event.preventDefault();
    if (!summaryDraft.trim()) {
      setSaveError('요약을 입력하세요.');
      return;
    }
    setSaving(true);
    setSaveError('');
    try {
      setDocument(await updateSummary(id, summaryDraft.trim(), keywordsDraft));
      setEditing(false);
    } catch (e) {
      setSaveError(e.message);
    } finally {
      setSaving(false);
    }
  };

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

      <h2>
        요약 {document.aiGenerated && <AiBadge />}
        {document.summary && !document.aiGenerated && (
          <span className="edited-badge">관리자 수정</span>
        )}
        {isAdmin && !editing && (
          <button type="button" className="ghost small-button" onClick={startEditing}>
            요약 수정
          </button>
        )}
      </h2>
      {editing ? (
        <form className="form summary-form" onSubmit={handleSave}>
          <label>
            요약
            <textarea
              value={summaryDraft}
              onChange={(event) => setSummaryDraft(event.target.value)}
              rows={6}
            />
          </label>
          <label>
            키워드 (쉼표로 구분)
            <input
              value={keywordsDraft}
              onChange={(event) => setKeywordsDraft(event.target.value)}
            />
          </label>
          <div className="button-row">
            <button type="submit" disabled={saving}>
              {saving ? '저장 중...' : '저장'}
            </button>
            <button
              type="button"
              className="ghost"
              onClick={() => setEditing(false)}
              disabled={saving}
            >
              취소
            </button>
          </div>
          {saveError && <p className="error">{saveError}</p>}
        </form>
      ) : (
        <>
          {document.summary ? (
            <p className="summary">{document.summary}</p>
          ) : (
            <p className="muted">요약이 없습니다. (OpenAI 키가 없거나 요약 생성에 실패)</p>
          )}

          <h2>키워드</h2>
          <p>{document.keywords || <span className="muted">없음</span>}</p>
        </>
      )}

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

      {isAdmin && (
        <button type="button" className="danger" onClick={handleDelete}>
          문서 삭제
        </button>
      )}
    </section>
  );
}
