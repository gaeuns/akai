const PREVIEW_LENGTH = 140;

// 답변 근거 한 건(문서명·페이지·원문 앞부분). 누르면 근거 원문 보기 팝업(UC-08)을 연다.
export default function EvidenceCard({ index, documentTitle, pageNo, content, onOpen }) {
  const preview =
    content.length > PREVIEW_LENGTH ? `${content.slice(0, PREVIEW_LENGTH)}…` : content;

  return (
    <button type="button" className="evidence-card" onClick={onOpen}>
      <span className="evidence-title">
        근거 {index} · {documentTitle} · {pageNo}쪽
      </span>
      <span className="evidence-preview">{preview}</span>
      <span className="evidence-more">원문 보기</span>
    </button>
  );
}
