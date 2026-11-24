import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { classifyDocument, uploadDocument } from '../api/documents.js';
import { createTag, fetchCategories, fetchTags } from '../api/tags.js';

// UC-02 · F-01·F-02: PDF·TXT 업로드 → 추출·분할·요약 후 상세 화면으로 이동.
// F-04: 업로드하면서 분류(필수)와 태그(선택)를 고른다. 상세 화면에서 나중에 바꿀 수 있다.
// 관리자 전용 화면 (권한 검사는 서버에서 다시 한다)
// 서버(spring.servlet.multipart.max-file-size)와 같은 한도. 큰 파일은 보내기 전에 막는다.
const MAX_FILE_SIZE = 20 * 1024 * 1024;

export default function UploadPage() {
  const navigate = useNavigate();
  const [file, setFile] = useState(null);
  const [title, setTitle] = useState('');
  const [categories, setCategories] = useState([]);
  const [tags, setTags] = useState([]);
  const [categoryId, setCategoryId] = useState('');
  const [tagIds, setTagIds] = useState([]);
  const [newTagName, setNewTagName] = useState('');
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([fetchCategories(), fetchTags()])
      .then(([categoryList, tagList]) => {
        setCategories(categoryList);
        setTags(tagList);
      })
      .catch((e) => setError(e.message));
  }, []);

  const toggleTag = (id) => {
    setTagIds((current) =>
      current.includes(id) ? current.filter((tagId) => tagId !== id) : [...current, id],
    );
  };

  // 목록에 없는 태그는 여기서 바로 만들고 선택해 둔다
  const handleAddTag = async () => {
    const name = newTagName.trim();
    if (!name) {
      return;
    }
    const existing = tags.find((tag) => tag.name === name);
    if (existing) {
      setTagIds((current) => (current.includes(existing.id) ? current : [...current, existing.id]));
      setNewTagName('');
      return;
    }
    setError('');
    try {
      const created = await createTag(name);
      setTags((current) => [...current, created]);
      setTagIds((current) => [...current, created.id]);
      setNewTagName('');
    } catch (e) {
      setError(e.message);
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!file) {
      setError('파일을 선택하세요.');
      return;
    }
    if (file.size > MAX_FILE_SIZE) {
      setError('파일이 너무 큽니다. 20MB 이하만 올릴 수 있습니다.');
      return;
    }
    if (!categoryId) {
      setError('분류를 고르세요.');
      return;
    }
    setUploading(true);
    setError('');
    try {
      const created = await uploadDocument(file, title.trim());
      try {
        await classifyDocument(created.id, categoryId, tagIds);
      } catch {
        // 문서는 이미 저장됐다. 분류·태그는 상세 화면에서 다시 지정하도록 안내한다
        navigate(`/documents/${created.id}`, { state: { classificationFailed: true } });
        return;
      }
      navigate(`/documents/${created.id}`);
    } catch (e) {
      setError(e.message);
    } finally {
      setUploading(false);
    }
  };

  return (
    <section>
      <p className="back-link">
        <Link to="/documents">문서 목록으로</Link>
      </p>
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
        <label>
          분류 (필수)
          <select
            value={categoryId}
            onChange={(event) => setCategoryId(event.target.value)}
            required
          >
            <option value="">분류를 고르세요</option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
          {categories.length === 0 && (
            <span className="muted small">
              등록된 분류가 없습니다. <Link to="/admin/tags">분류와 태그</Link>에서 먼저 추가하세요.
            </span>
          )}
        </label>
        <fieldset className="upload-tags">
          <legend>태그 (선택)</legend>
          {tags.length > 0 && (
            <div className="tag-options">
              {tags.map((tag) => (
                <label key={tag.id} className={tagIds.includes(tag.id) ? 'selected' : ''}>
                  <input
                    type="checkbox"
                    checked={tagIds.includes(tag.id)}
                    onChange={() => toggleTag(tag.id)}
                  />
                  #{tag.name}
                </label>
              ))}
            </div>
          )}
          <div className="tag-add">
            <input
              value={newTagName}
              onChange={(event) => setNewTagName(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === 'Enter') {
                  event.preventDefault();
                  handleAddTag();
                }
              }}
              placeholder="새 태그 이름"
              aria-label="새 태그 이름"
            />
            <button type="button" className="ghost" onClick={handleAddTag}>
              태그 추가
            </button>
          </div>
        </fieldset>
        <button type="submit" disabled={uploading}>
          {uploading ? '처리 중... (추출·분할·요약)' : '업로드'}
        </button>
      </form>
      {error && <p className="error">{error}</p>}
    </section>
  );
}
