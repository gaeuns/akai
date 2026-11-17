import { useEffect } from 'react';
import { Link } from 'react-router-dom';

// UC-08 · F-03: 근거 원문 보기 팝업. 바깥을 누르거나 Esc를 누르면 닫힌다.
export default function EvidenceModal({ evidence, onClose }) {
  useEffect(() => {
    const handleKeyDown = (event) => {
      if (event.key === 'Escape') {
        onClose();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div
        className="modal"
        role="dialog"
        aria-modal="true"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="modal-header">
          <strong>
            {evidence.documentTitle} · {evidence.pageNo}쪽
          </strong>
          <button type="button" className="ghost" onClick={onClose}>
            닫기
          </button>
        </div>
        <p className="chunk-content">{evidence.content}</p>
        <Link to={`/documents/${evidence.documentId}`}>문서 상세로 이동</Link>
      </div>
    </div>
  );
}
