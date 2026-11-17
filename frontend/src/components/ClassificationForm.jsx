import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { classifyDocument } from '../api/documents.js';
import { fetchCategories, fetchTags } from '../api/tags.js';

// UC-03 · F-04: 문서 상세에서 관리자가 분류를 고르고 태그를 붙인다 (권한 검사는 서버에서 다시 한다)
export default function ClassificationForm({ document, onSaved }) {
  const [categories, setCategories] = useState([]);
  const [tags, setTags] = useState([]);
  const [categoryId, setCategoryId] = useState(
    document.category ? String(document.category.id) : '',
  );
  const [tagIds, setTagIds] = useState(document.tags.map((tag) => tag.id));
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');
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
    setMessage('');
    setTagIds((current) =>
      current.includes(id) ? current.filter((tagId) => tagId !== id) : [...current, id],
    );
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    setMessage('');
    try {
      const saved = await classifyDocument(document.id, categoryId, tagIds);
      onSaved(saved);
      setMessage('분류·태그를 저장했습니다.');
    } catch (e) {
      setError(e.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <form className="classification-form" onSubmit={handleSubmit}>
      <label>
        분류
        <select
          value={categoryId}
          onChange={(event) => {
            setCategoryId(event.target.value);
            setMessage('');
          }}
        >
          <option value="">미분류</option>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </select>
      </label>
      <fieldset>
        <legend>태그</legend>
        {tags.length === 0 ? (
          <span className="muted">
            등록된 태그가 없습니다. <Link to="/admin/tags">분류·태그 관리</Link>에서 추가하세요.
          </span>
        ) : (
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
      </fieldset>
      <div className="classification-footer">
        <button type="submit" disabled={saving}>
          {saving ? '저장 중...' : '분류·태그 저장'}
        </button>
        {message && <span className="success">{message}</span>}
      </div>
      {error && <p className="error">{error}</p>}
    </form>
  );
}
