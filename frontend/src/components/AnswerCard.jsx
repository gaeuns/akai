import { useState } from 'react';
import { Link } from 'react-router-dom';
import EvidenceCard from './EvidenceCard.jsx';
import { formatDateTime } from '../utils/format.js';

// 질문 하나의 답변과 근거. 질문하기(UC-07)와 질의 이력(UC-09)에서 함께 쓴다.
// 왼쪽에 AI 답, 오른쪽에 근거 원문을 두고 같은 번호로 잇는다.
// compact(질의 이력)에서는 근거를 접어 두고, 누르면 펼친다.
export default function AnswerCard({ answer, onOpenEvidence, compact = false }) {
  const evidenceDeleted = answer.found && answer.evidences.length === 0;
  const [showEvidence, setShowEvidence] = useState(!compact);
  const evidenceId = `evidence-${answer.questionId}`;

  return (
    <article className={compact ? 'answer-card compact' : 'answer-card'}>
      <div className="answer-meta">
        <h2 className="answer-question">{answer.question}</h2>
        <span className="muted small">{formatDateTime(answer.askedAt)}</span>
      </div>

      {!answer.found ? (
        <div className="answer-not-found" role="status">
          <strong>문서에서 찾을 수 없음</strong>
          <p>올라온 문서에서 이 질문의 근거를 찾지 못해 답하지 않았습니다.</p>
          <p className="muted small">
            다른 표현으로 다시 물어보거나, <Link to="/documents">문서 목록</Link>에서 관련 문서가
            있는지 확인하세요.
          </p>
        </div>
      ) : (
        <div className="answer-layout">
          <section className="answer-body" aria-label="답변">
            <div className="answer-label">
              {answer.aiGenerated && <span className="ai-badge">AI 생성 답변</span>}
              {answer.evidences.length > 0 && (
                <span className="muted small">근거 {answer.evidences.length}건에서만 만든 답</span>
              )}
            </div>
            <p className="summary answer-text">{answer.answer}</p>
            {compact && answer.evidences.length > 0 && (
              <button
                type="button"
                className="ghost evidence-toggle"
                aria-expanded={showEvidence}
                aria-controls={evidenceId}
                onClick={() => setShowEvidence((shown) => !shown)}
              >
                {showEvidence ? '근거 원문 접기' : `근거 원문 보기 (${answer.evidences.length})`}
              </button>
            )}
            {!compact && answer.evidences.length > 0 && (
              <p className="evidence-refs">
                근거
                {answer.evidences.map((evidence, index) => (
                  <span key={evidence.chunkId} className="evidence-number" aria-hidden="true">
                    {index + 1}
                  </span>
                ))}
              </p>
            )}
            {evidenceDeleted && (
              <p className="muted small">근거 문서가 삭제되어 근거를 표시할 수 없습니다.</p>
            )}
          </section>

          {answer.evidences.length > 0 && showEvidence && (
            <aside id={evidenceId} className="evidences" aria-label="근거 원문">
              <div className="evidences-head">
                <h3>근거 원문</h3>
                <span className="small">문서에 적힌 그대로</span>
              </div>
              {answer.evidences.map((evidence, index) => (
                <EvidenceCard
                  key={evidence.chunkId}
                  index={index + 1}
                  documentTitle={evidence.documentTitle}
                  pageNo={evidence.pageNo}
                  content={evidence.content}
                  question={answer.question}
                  onOpen={() =>
                    onOpenEvidence({ ...evidence, index: index + 1, question: answer.question })
                  }
                />
              ))}
              <p className="small evidences-note">밑줄은 질문과 겹치는 말입니다.</p>
            </aside>
          )}
        </div>
      )}
    </article>
  );
}
