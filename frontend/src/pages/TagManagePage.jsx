import { useCallback, useEffect, useState } from 'react';
import {
  createCategory,
  createTag,
  deleteCategory,
  deleteTag,
  fetchCategories,
  fetchTags,
  renameCategory,
  renameTag,
} from '../api/tags.js';

// UC-03 · F-04: 분류·태그 추가·수정·삭제 (관리자 전용 화면, 권한 검사는 서버에서 다시 한다)
// 분류·태그는 문서와 집합 관계라 지워도 문서는 남는다. 문서에 붙이는 것은 문서 상세 화면에서 한다.
export default function TagManagePage() {
  return (
    <section>
      <h1>분류·태그 관리</h1>
      <p className="muted">
        분류·태그를 지워도 문서는 삭제되지 않고 연결만 끊어집니다. 문서에 붙이는 것은 문서 상세
        화면에서 합니다.
      </p>
      <div className="manage-grid">
        <NameListEditor
          kind="분류"
          deleteNotice="이 분류의 문서는 미분류가 됩니다."
          fetchAll={fetchCategories}
          create={createCategory}
          rename={renameCategory}
          remove={deleteCategory}
        />
        <NameListEditor
          kind="태그"
          deleteNotice="문서에서 이 태그만 떨어집니다."
          fetchAll={fetchTags}
          create={createTag}
          rename={renameTag}
          remove={deleteTag}
        />
      </div>
    </section>
  );
}

function NameListEditor({ kind, deleteNotice, fetchAll, create, rename, remove }) {
  const [items, setItems] = useState([]);
  const [newName, setNewName] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [editingName, setEditingName] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    try {
      setItems(await fetchAll());
    } catch (e) {
      setError(e.message);
    }
  }, [fetchAll]);

  useEffect(() => {
    load();
  }, [load]);

  // 요청 하나를 보내고 목록을 다시 읽는다. 실패하면 서버 안내 문구를 보여 준다
  const run = async (action) => {
    setBusy(true);
    setError('');
    try {
      await action();
      await load();
      return true;
    } catch (e) {
      setError(e.message);
      return false;
    } finally {
      setBusy(false);
    }
  };

  const handleCreate = async (event) => {
    event.preventDefault();
    if (!newName.trim()) {
      setError(`${kind} 이름을 입력하세요.`);
      return;
    }
    if (await run(() => create(newName.trim()))) {
      setNewName('');
    }
  };

  const startEdit = (item) => {
    setEditingId(item.id);
    setEditingName(item.name);
    setError('');
  };

  const handleRename = async (event) => {
    event.preventDefault();
    if (!editingName.trim()) {
      setError(`${kind} 이름을 입력하세요.`);
      return;
    }
    if (await run(() => rename(editingId, editingName.trim()))) {
      setEditingId(null);
    }
  };

  const handleDelete = (item) => {
    if (window.confirm(`${kind} ‘${item.name}’을(를) 삭제할까요? ${deleteNotice}`)) {
      run(() => remove(item.id));
    }
  };

  return (
    <div className="manage-panel">
      <h2>
        {kind} ({items.length})
      </h2>
      <form className="search" onSubmit={handleCreate}>
        <input
          value={newName}
          onChange={(event) => setNewName(event.target.value)}
          placeholder={`새 ${kind} 이름 (50자 이하)`}
          maxLength={50}
        />
        <button type="submit" disabled={busy}>
          추가
        </button>
      </form>
      {error && <p className="error">{error}</p>}
      {items.length === 0 ? (
        <p className="muted">등록된 {kind}가 없습니다.</p>
      ) : (
        <ul className="name-list">
          {items.map((item) =>
            editingId === item.id ? (
              <li key={item.id}>
                <form className="name-edit" onSubmit={handleRename}>
                  <input
                    value={editingName}
                    onChange={(event) => setEditingName(event.target.value)}
                    maxLength={50}
                    aria-label={`${kind} 이름`}
                    autoFocus
                  />
                  <button type="submit" disabled={busy}>
                    저장
                  </button>
                  <button type="button" className="ghost" onClick={() => setEditingId(null)}>
                    취소
                  </button>
                </form>
              </li>
            ) : (
              <li key={item.id}>
                <span>{item.name}</span>
                <span className="name-actions">
                  <button type="button" className="ghost" onClick={() => startEdit(item)}>
                    수정
                  </button>
                  <button
                    type="button"
                    className="ghost danger-text"
                    onClick={() => handleDelete(item)}
                    disabled={busy}
                  >
                    삭제
                  </button>
                </span>
              </li>
            ),
          )}
        </ul>
      )}
    </div>
  );
}
