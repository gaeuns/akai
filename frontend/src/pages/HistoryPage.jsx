import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import AnswerCard from '../components/AnswerCard.jsx';
import EvidenceModal from '../components/EvidenceModal.jsx';
import { fetchHistory } from '../api/questions.js';

// UC-09 · F-05: 내 질문·답변·근거·일시 목록 (최신순). 근거를 누르면 원문 팝업(UC-08).
export default function HistoryPage() {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [openEvidence, setOpenEvidence] = useState(null);

  useEffect(() => {
    const load = async () => {
      try {
        setHistory(await fetchHistory());
      } catch (e) {
        setError(e.message);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  return (
    <section>
      <h1>질의 이력</h1>
      {error && <p className="error">{error}</p>}
      {loading && <p>불러오는 중...</p>}
      {!loading && !error && history.length === 0 && (
        <p className="muted">
          아직 질문한 기록이 없습니다. <Link to="/qa">질문하기</Link>에서 질문해 보세요.
        </p>
      )}
      {history.length > 0 && (
        <>
          <p className="muted">총 {history.length}건</p>
          <div className="history">
            {history.map((answer) => (
              <AnswerCard
                compact
                key={answer.questionId}
                answer={answer}
                onOpenEvidence={setOpenEvidence}
              />
            ))}
          </div>
        </>
      )}
      {openEvidence && (
        <EvidenceModal evidence={openEvidence} onClose={() => setOpenEvidence(null)} />
      )}
    </section>
  );
}
