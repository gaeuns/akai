import { splitByTerms } from '../utils/highlight.js';

// F-11: 질문과 관련된 말을 형광펜(<mark>)으로 표시한다
export default function Highlighted({ text, terms }) {
  return splitByTerms(text, terms).map((part, index) =>
    part.match ? <mark key={index}>{part.text}</mark> : <span key={index}>{part.text}</span>,
  );
}
