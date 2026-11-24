import { useCallback, useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import AiBadge from '../components/AiBadge.jsx';
import { compareDocuments } from '../api/comparisons.js';

// F-07: 문서 목록에서 고른 문서 2~5개의 공통점·차이점 요약 (AI 생성)
export default function ComparePage() {
  const [searchParams] = useSearchParams();
  const ids = (searchParams.get('ids') ?? '')
    .split(',')
    .map(Number)
    .filter((id) => Number.isInteger(id) && id > 0);
  const idsKey = ids.join(',');
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      setResult(await compareDocuments(idsKey.split(',').map(Number)));
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [idsKey]);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <section>
      <h1>문서 비교 요약</h1>
      <p>
        <Link to="/documents">← 문서 목록</Link>
      </p>

      {loading && <p>비교 요약 생성 중... (문서 수에 따라 몇 초 걸립니다)</p>}
      {error && (
        <div className="error">
          {error}
          <button type="button" className="ghost retry" onClick={load} disabled={loading}>
            다시 시도
          </button>
        </div>
      )}

      {result && !loading && (
        <>
          <h2>비교한 문서 ({result.documents.length})</h2>
          <ul className="compared-documents">
            {result.documents.map((document) => (
              <li key={document.id}>
                <Link to={`/documents/${document.id}`}>{document.title}</Link>
                {!document.usedSummary && (
                  <span className="muted small"> · 요약이 없어 본문 앞부분으로 비교</span>
                )}
              </li>
            ))}
          </ul>

          <h2>공통점·차이점 {result.aiGenerated && <AiBadge />}</h2>
          <p className="summary comparison">{result.comparison}</p>
          <p className="muted small">
            각 문서의 저장된 요약을 바탕으로 AI가 정리했습니다. 정확한 내용은 문서 원문을
            확인하세요.
          </p>
        </>
      )}
    </section>
  );
}
