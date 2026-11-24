import AiBadge from './AiBadge.jsx';
import EvidenceCard from './EvidenceCard.jsx';
import { formatDateTime } from '../utils/format.js';

// 질문 하나의 답변과 근거. 질문하기(UC-07)와 질의 이력(UC-09)에서 함께 쓴다.
export default function AnswerCard({ answer, onOpenEvidence }) {
  const evidenceDeleted = answer.found && answer.evidences.length === 0;

  return (
    <article className="answer-card">
      <div className="answer-meta">
        <span className="answer-question">Q. {answer.question}</span>
        <span className="muted small">{formatDateTime(answer.askedAt)}</span>
      </div>

      <h2>답변 {answer.aiGenerated && <AiBadge />}</h2>
      <p className={answer.found ? 'summary' : 'answer-not-found'}>{answer.answer}</p>
      {!answer.found && (
        <p className="muted small">
          업로드된 문서에서 관련 내용을 찾지 못했습니다. 다른 표현으로 질문해 보세요.
        </p>
      )}
      {evidenceDeleted && (
        <p className="muted small">근거 문서가 삭제되어 근거를 표시할 수 없습니다.</p>
      )}

      {answer.evidences.length > 0 && (
        <>
          <h2>근거 ({answer.evidences.length})</h2>
          <div className="evidences">
            {answer.evidences.map((evidence, index) => (
              <EvidenceCard
                key={evidence.chunkId}
                index={index + 1}
                documentTitle={evidence.documentTitle}
                pageNo={evidence.pageNo}
                content={evidence.content}
                question={answer.question}
                onOpen={() => onOpenEvidence({ ...evidence, question: answer.question })}
              />
            ))}
          </div>
        </>
      )}
    </article>
  );
}
