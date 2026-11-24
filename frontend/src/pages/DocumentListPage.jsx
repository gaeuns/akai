import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AiBadge from '../components/AiBadge.jsx';
import TagChips from '../components/TagChips.jsx';
import { fetchDocuments } from '../api/documents.js';
import { fetchCategories } from '../api/tags.js';
import { useAuth } from '../utils/auth.js';
import { formatDateTime } from '../utils/format.js';

// UC-05 · F-04: 제목·키워드·태그 검색과 분류 필터. F-07: 문서를 2~5개 골라 비교 요약
const MAX_COMPARE = 5;

export default function DocumentListPage() {
  const { isAdmin } = useAuth();
  const navigate = useNavigate();
  const [selectedIds, setSelectedIds] = useState([]);
  const [keyword, setKeyword] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [categories, setCategories] = useState([]);
  const [documents, setDocuments] = useState([]);
  const [searched, setSearched] = useState({ keyword: '', categoryId: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const load = useCallback(async (searchKeyword, searchCategoryId) => {
    setLoading(true);
    setError('');
    try {
      setDocuments(await fetchDocuments(searchKeyword, searchCategoryId));
      setSearched({ keyword: searchKeyword, categoryId: searchCategoryId });
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load('', '');
    fetchCategories()
      .then(setCategories)
      .catch(() => setCategories([]));
  }, [load]);

  const handleSubmit = (event) => {
    event.preventDefault();
    load(keyword.trim(), categoryId);
  };

  const handleCategoryChange = (event) => {
    setCategoryId(event.target.value);
    load(keyword.trim(), event.target.value);
  };

  const handleTagSelect = (tagName) => {
    setKeyword(tagName);
    load(tagName, categoryId);
  };

  const toggleSelected = (id) => {
    setSelectedIds((current) =>
      current.includes(id) ? current.filter((selected) => selected !== id) : [...current, id],
    );
  };

  const handleCompare = () => {
    navigate(`/compare?ids=${selectedIds.join(',')}`);
  };

  const filtered = searched.keyword || searched.categoryId;
  const categoryName = categories.find((c) => String(c.id) === searched.categoryId)?.name;
  const conditions = [
    searched.keyword && `‘${searched.keyword}’`,
    categoryName && `분류 ‘${categoryName}’`,
  ]
    .filter(Boolean)
    .join(', ');

  return (
    <section>
      <div className="page-head">
        <h1>
          문서 {!loading && !error && <span className="page-count">{documents.length}개</span>}
        </h1>
        {isAdmin && (
          <Link to="/admin/upload" className="button-link secondary">
            + 문서 업로드
          </Link>
        )}
      </div>
      <form className="search" onSubmit={handleSubmit}>
        <select value={categoryId} onChange={handleCategoryChange} aria-label="분류">
          <option value="">전체 분류</option>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </select>
        <input
          value={keyword}
          onChange={(event) => setKeyword(event.target.value)}
          placeholder="제목, 키워드 또는 태그로 검색"
        />
        <button type="submit">검색</button>
      </form>

      {error && <p className="error">{error}</p>}
      {loading && <p>불러오는 중...</p>}
      {!loading && !error && documents.length === 0 && filtered && (
        <p className="muted">{conditions}에 맞는 문서가 없습니다. 다른 조건으로 찾아보세요.</p>
      )}
      {!loading && !error && documents.length === 0 && !filtered && (
        <p className="muted">
          문서가 없습니다.{' '}
          {isAdmin ? (
            <>
              <Link to="/admin/upload">문서 업로드</Link>에서 올려 보세요.
            </>
          ) : (
            '관리자가 문서를 올리면 여기에 표시됩니다.'
          )}
        </p>
      )}

      {documents.length > 0 && (
        <div className="compare-bar">
          <span className="muted small">
            비교할 문서를 2~{MAX_COMPARE}개 고르세요 (선택 {selectedIds.length}개)
          </span>
          <button
            type="button"
            onClick={handleCompare}
            disabled={selectedIds.length < 2 || selectedIds.length > MAX_COMPARE}
          >
            선택한 문서 비교
          </button>
        </div>
      )}

      {documents.length > 0 && (
        <table className="table">
          <thead>
            <tr>
              <th aria-label="비교 선택" />
              <th>제목</th>
              <th>분류</th>
              <th>형식</th>
              <th>키워드·태그</th>
              <th>조각</th>
              <th>등록일</th>
            </tr>
          </thead>
          <tbody>
            {documents.map((document) => (
              <tr key={document.id}>
                <td>
                  <input
                    type="checkbox"
                    aria-label={`${document.title} 비교 선택`}
                    checked={selectedIds.includes(document.id)}
                    disabled={
                      !selectedIds.includes(document.id) && selectedIds.length >= MAX_COMPARE
                    }
                    onChange={() => toggleSelected(document.id)}
                  />
                </td>
                <td>
                  <Link to={`/documents/${document.id}`}>{document.title}</Link>
                </td>
                <td>{document.category?.name ?? <span className="muted">미분류</span>}</td>
                <td>{document.fileType}</td>
                <td>
                  {document.keywords ? (
                    <div>
                      {document.keywords} {document.aiGenerated && <AiBadge />}
                    </div>
                  ) : (
                    <div className="muted">키워드 없음</div>
                  )}
                  <TagChips tags={document.tags} onSelect={handleTagSelect} />
                </td>
                <td>{document.chunkCount}</td>
                <td>{formatDateTime(document.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
