import { useState } from 'react';
import AiBadge from '../components/AiBadge.jsx';
import EvidenceCard from '../components/EvidenceCard.jsx';
import EvidenceModal from '../components/EvidenceModal.jsx';
import { askQuestion } from '../api/questions.js';

// UC-07 · F-03: 질문 → 답변(AI 생성 표시) + 근거 카드. 근거를 누르면 원문 팝업(UC-08).
// 서버(QaService.MAX_QUESTION_LENGTH)와 같은 한도
const MAX_QUESTION_LENGTH = 1000;

export default function QaPage() {
  const [question, setQuestion] = useState('');
  const [result, setResult] = useState(null);
  const [asking, setAsking] = useState(false);
  const [error, setError] = useState('');
  const [failedQuestion, setFailedQuestion] = useState('');
  const [openEvidence, setOpenEvidence] = useState(null);

  const submit = async (text) => {
    if (!text) {
      setError('질문을 입력하세요.');
      return;
    }
    if (text.length > MAX_QUESTION_LENGTH) {
      setError(`질문은 ${MAX_QUESTION_LENGTH}자 이내로 입력하세요.`);
      return;
    }
    setAsking(true);
    setError('');
    setFailedQuestion('');
    try {
      setResult(await askQuestion(text));
    } catch (e) {
      setError(e.message);
      setFailedQuestion(text);
    } finally {
      setAsking(false);
    }
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    submit(question.trim());
  };

  return (
    <section>
      <h1>질문하기</h1>
      <p className="muted">업로드된 문서 내용만 근거로 답합니다.</p>

      <form className="qa-form" onSubmit={handleSubmit}>
        <textarea
          value={question}
          onChange={(event) => setQuestion(event.target.value)}
          placeholder="예: 휴학은 최대 몇 학기까지 가능한가요?"
          rows={3}
        />
        <div className="qa-form-footer">
          <span className="muted small">
            {question.length} / {MAX_QUESTION_LENGTH}
          </span>
          <button type="submit" disabled={asking}>
            {asking ? '답변 생성 중...' : '질문'}
          </button>
        </div>
      </form>

      {error && (
        <div className="error">
          {error}
          {failedQuestion && (
            <button
              type="button"
              className="ghost retry"
              onClick={() => submit(failedQuestion)}
              disabled={asking}
            >
              다시 시도
            </button>
          )}
        </div>
      )}

      {result && (
        <div className="qa-result">
          <p className="muted">Q. {result.question}</p>
          <h2>답변 {result.aiGenerated && <AiBadge />}</h2>
          <p className={result.found ? 'summary' : 'answer-not-found'}>{result.answer}</p>
          {!result.found && (
            <p className="muted small">
              업로드된 문서에서 관련 내용을 찾지 못했습니다. 다른 표현으로 질문해 보세요.
            </p>
          )}

          {result.evidences.length > 0 && (
            <>
              <h2>근거 ({result.evidences.length})</h2>
              <div className="evidences">
                {result.evidences.map((evidence, index) => (
                  <EvidenceCard
                    key={evidence.chunkId}
                    index={index + 1}
                    documentTitle={evidence.documentTitle}
                    pageNo={evidence.pageNo}
                    content={evidence.content}
                    onOpen={() => setOpenEvidence(evidence)}
                  />
                ))}
              </div>
            </>
          )}
        </div>
      )}

      {openEvidence && (
        <EvidenceModal evidence={openEvidence} onClose={() => setOpenEvidence(null)} />
      )}
    </section>
  );
}
