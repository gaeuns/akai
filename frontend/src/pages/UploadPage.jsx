import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { uploadDocument } from '../api/documents.js';

// UC-02 · F-01·F-02: PDF·TXT 업로드 → 추출·분할·요약 후 상세 화면으로 이동.
// TODO(F-08): 관리자에게만 메뉴 표시 (권한 검사는 서버에서 다시 한다)
export default function UploadPage() {
  const navigate = useNavigate();
  const [file, setFile] = useState(null);
  const [title, setTitle] = useState('');
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!file) {
      setError('파일을 선택하세요.');
      return;
    }
    setUploading(true);
    setError('');
    try {
      const created = await uploadDocument(file, title.trim());
      navigate(`/documents/${created.id}`);
    } catch (e) {
      setError(e.message);
    } finally {
      setUploading(false);
    }
  };

  return (
    <section>
      <h1>문서 업로드</h1>
      <form className="form" onSubmit={handleSubmit}>
        <label>
          파일 (PDF·TXT, 20MB 이하)
          <input
            type="file"
            accept=".pdf,.txt"
            onChange={(event) => setFile(event.target.files[0] ?? null)}
          />
        </label>
        <label>
          제목 (비우면 파일 이름)
          <input value={title} onChange={(event) => setTitle(event.target.value)} />
        </label>
        <button type="submit" disabled={uploading}>
          {uploading ? '처리 중... (추출·분할·요약)' : '업로드'}
        </button>
      </form>
      {error && <p className="error">{error}</p>}
    </section>
  );
}
