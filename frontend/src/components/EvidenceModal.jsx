import { useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import Highlighted from './Highlighted.jsx';
import { extractTerms } from '../utils/highlight.js';

// UC-08 · F-03: 근거 원문 보기 팝업. 바깥을 누르거나 Esc·닫기를 누르면 닫힌다.
// 브라우저 기본 <dialog>를 써서 키보드 포커스가 팝업 안에 머물고, 닫으면 누른 근거로 돌아간다.
// F-11: evidence.question이 있으면 질문과 겹치는 말에 밑줄을 친다.
export default function EvidenceModal({ evidence, onClose }) {
  const dialogRef = useRef(null);

  useEffect(() => {
    const dialog = dialogRef.current;
    const opener = document.activeElement;
    if (!dialog.open) {
      dialog.showModal();
    }
    return () => {
      if (dialog.open) {
        dialog.close();
      }
      if (opener instanceof HTMLElement) {
        opener.focus();
      }
    };
  }, []);

  const handleCancel = (event) => {
    event.preventDefault();
    onClose();
  };

  // 팝업 바깥(배경)을 누르면 dialog 자신이 클릭 대상이 된다
  const handleClick = (event) => {
    if (event.target === dialogRef.current) {
      onClose();
    }
  };

  return (
    <dialog
      ref={dialogRef}
      className="modal"
      aria-labelledby="evidence-modal-title"
      onCancel={handleCancel}
      onClick={handleClick}
    >
      <div className="modal-body">
        <div className="modal-header">
          <h2 id="evidence-modal-title">
            {evidence.index && (
              <span className="evidence-number" aria-hidden="true">
                {evidence.index}
              </span>
            )}
            {evidence.documentTitle} <span className="muted">{evidence.pageNo}쪽</span>
          </h2>
          <button type="button" className="ghost" onClick={onClose}>
            닫기
          </button>
        </div>
        {evidence.question && (
          <p className="muted small">질문: {evidence.question}. 밑줄은 질문과 겹치는 말입니다.</p>
        )}
        <p className="chunk-content">
          <Highlighted text={evidence.content} terms={extractTerms(evidence.question)} />
        </p>
        <Link to={`/documents/${evidence.documentId}`}>이 문서 전체 보기</Link>
      </div>
    </dialog>
  );
}
