import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { fetchStatistics } from '../api/admin.js';
import { formatDateTime } from '../utils/format.js';

// F-09 (관리자): 저장된 질문·답변으로 계산한 품질 지표. NF-01(찾을 수 없음 응답), NF-02(5초 이내 응답)
function formatPercent(value) {
  return value == null ? '—' : `${value.toFixed(1)}%`;
}

const STRATEGY_NAMES = { keyword: '키워드 검색', embedding: '임베딩 검색' };

export default function DashboardPage() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      setStats(await fetchStatistics());
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  if (loading && !stats) {
    return <p>불러오는 중...</p>;
  }
  if (error) {
    return <p className="error">{error}</p>;
  }

  const maxUsage = Math.max(1, ...stats.topDocuments.map((doc) => doc.evidenceCount));

  return (
    <section>
      <div className="dashboard-head">
        <h1>품질 지표</h1>
        <button type="button" className="ghost" onClick={load} disabled={loading}>
          새로고침
        </button>
      </div>
      <p className="muted small">
        검색 방식: <strong>{STRATEGY_NAMES[stats.searchStrategy] ?? stats.searchStrategy}</strong> ·
        조각 {stats.chunkCount}개 중 임베딩 계산 {stats.embeddedChunkCount}개
      </p>

      <div className="stat-cards">
        <StatCard label="문서" value={stats.documentCount} />
        <StatCard label="질문" value={stats.questionCount} />
        <StatCard
          label="찾을 수 없음 응답"
          value={formatPercent(stats.notFoundRate)}
          note={`${stats.notFoundCount}건 · 근거가 없으면 AI를 부르지 않음`}
        />
        <StatCard
          label="평균 응답 시간"
          value={
            stats.averageResponseSeconds == null
              ? '—'
              : `${stats.averageResponseSeconds.toFixed(1)}초`
          }
          note="검색~답변 (목표 5초 이내)"
        />
        <StatCard
          label="5초 이내 응답"
          value={formatPercent(stats.within5sRate)}
          note="NF-02 성능 목표"
        />
      </div>

      <h2>근거로 많이 쓰인 문서</h2>
      {stats.topDocuments.length === 0 ? (
        <p className="muted">아직 근거로 쓰인 문서가 없습니다.</p>
      ) : (
        <ul className="usage-list">
          {stats.topDocuments.map((doc) => (
            <li key={doc.documentId}>
              <Link to={`/documents/${doc.documentId}`}>{doc.title}</Link>
              <div className="score-bar">
                <span style={{ width: `${(doc.evidenceCount / maxUsage) * 100}%` }} />
              </div>
              <span className="muted small">근거 {doc.evidenceCount}회</span>
            </li>
          ))}
        </ul>
      )}

      <h2>최근 문서에서 답을 찾지 못한 질문</h2>
      <p className="muted small">자주 나오는 질문이면 관련 문서를 올리면 됩니다.</p>
      {stats.recentNotFound.length === 0 ? (
        <p className="muted">없습니다.</p>
      ) : (
        <ul className="not-found-list">
          {stats.recentNotFound.map((item, index) => (
            <li key={index}>
              {item.question} <span className="muted small">· {formatDateTime(item.askedAt)}</span>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

function StatCard({ label, value, note }) {
  return (
    <div className="stat-card">
      <span className="muted small">{label}</span>
      <strong>{value}</strong>
      {note && <span className="muted small">{note}</span>}
    </div>
  );
}
