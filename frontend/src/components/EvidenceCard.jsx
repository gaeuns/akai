// 답변 근거 한 건(문서명·페이지·원문). TODO(F-03): 클릭 시 근거 원문 보기 팝업(UC-08)
export default function EvidenceCard({ documentTitle, pageNo, content }) {
  return (
    <div>
      <strong>{documentTitle}</strong> · {pageNo}쪽<p>{content}</p>
    </div>
  );
}
