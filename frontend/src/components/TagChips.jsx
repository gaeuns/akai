// 문서에 붙은 태그 표시. onSelect가 있으면 태그를 눌러 그 태그로 검색한다
export default function TagChips({ tags, onSelect }) {
  if (!tags || tags.length === 0) {
    return null;
  }
  return (
    <span className="tag-chips">
      {tags.map((tag) =>
        onSelect ? (
          <button
            key={tag.id}
            type="button"
            className="tag-chip"
            onClick={() => onSelect(tag.name)}
            title={`‘${tag.name}’ 태그로 검색`}
          >
            #{tag.name}
          </button>
        ) : (
          <span key={tag.id} className="tag-chip">
            #{tag.name}
          </span>
        ),
      )}
    </span>
  );
}
