import Highlighted from './Highlighted.jsx';
import { extractTerms } from '../utils/highlight.js';

const PREVIEW_LENGTH = 160;
const LEAD = 40;

// 질문 단어가 가장 많이 모인 구간을 골라 원문 일부를 보여 준다. 없으면 앞부분.
function makePreview(content, terms) {
  const lower = content.toLowerCase();
  const hits = [];
  terms.forEach((term) => {
    for (let at = lower.indexOf(term); at >= 0; at = lower.indexOf(term, at + term.length)) {
      hits.push({ at, term });
    }
  });
  let bestStart = 0;
  let bestScore = 0;
  hits.forEach(({ at }) => {
    const windowStart = Math.max(0, at - LEAD);
    const windowEnd = windowStart + PREVIEW_LENGTH;
    const inside = hits.filter((hit) => hit.at >= windowStart && hit.at < windowEnd);
    const score = new Set(inside.map((hit) => hit.term)).size * 10 + inside.length;
    if (score > bestScore) {
      bestScore = score;
      bestStart = windowStart;
    }
  });
  const end = Math.min(content.length, bestStart + PREVIEW_LENGTH);
  return `${bestStart > 0 ? '…' : ''}${content.slice(bestStart, end)}${end < content.length ? '…' : ''}`;
}

export default function EvidenceCard({ index, documentTitle, pageNo, content, question, onOpen }) {
  const terms = extractTerms(question);
  const preview = makePreview(content, terms);

  return (
    <button
      type="button"
      className="evidence-card"
      onClick={onOpen}
      aria-label={`근거 ${index}, ${documentTitle} ${pageNo}쪽, 원문 보기`}
    >
      <span className="evidence-title">
        <span className="evidence-number" aria-hidden="true">
          {index}
        </span>
        <span className="evidence-document">{documentTitle}</span>
        <span className="evidence-page">{pageNo}쪽</span>
      </span>
      <span className="evidence-preview">
        <Highlighted text={preview} terms={terms} />
      </span>
      <span className="evidence-more">원문 전체 보기</span>
    </button>
  );
}
