import { useState } from 'react';
import { Link } from 'react-router-dom';
import { compareSearch } from '../api/admin.js';

// F-10 (관리자): 같은 질문을 키워드 검색과 임베딩 검색으로 찾아 점수와 함께 나란히 보여 준다.
// 답변은 만들지 않고 검색만 한다. 질문하기에서 쓰는 방식은 서버 설정(app.search.strategy)을 따른다.
const PREVIEW_LENGTH = 160;

const EXAMPLES = ["'A' 학점은 몇퍼센트가 받을 수 있나?", '휴학은 최대 몇 학기까지 가능한가요?'];

export default function SearchComparePage() {
  const [question, setQuestion] = useState('');
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const search = async (text) => {
    if (!text.trim()) {
      setError('질문을 입력하세요.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      setResult(await compareSearch(text.trim()));
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    search(question);
  };

  const runExample = (example) => {
    setQuestion(example);
    search(example);
  };

  return (
    <section>
      <h1>검색 방식 비교</h1>
      <p className="muted">
        같은 질문을 키워드 검색과 임베딩 검색으로 찾아 비교합니다. 기준을 넘은 조각만 답변의 근거로
        쓰이고, 하나도 없으면 &quot;문서에서 찾을 수 없음&quot;으로 답합니다.
      </p>

      <form className="search" onSubmit={handleSubmit}>
        <input
          value={question}
          onChange={(event) => setQuestion(event.target.value)}
          placeholder="비교할 질문"
        />
        <button type="submit" disabled={loading}>
          {loading ? '검색 중...' : '비교'}
        </button>
      </form>
      <p className="muted small">
        예시:{' '}
        {EXAMPLES.map((example) => (
          <button
            key={example}
            type="button"
            className="ghost small-button"
            onClick={() => runExample(example)}
          >
            {example}
          </button>
        ))}
      </p>

      {error && <p className="error">{error}</p>}

      {result && (
        <div className="strategy-grid">
          <StrategyColumn title="키워드 검색" result={result.keyword} />
          <StrategyColumn title="임베딩 검색" result={result.embedding} />
        </div>
      )}
    </section>
  );
}

function StrategyColumn({ title, result }) {
  const relevantCount = result.results.filter((item) => item.relevant).length;

  return (
    <div className="strategy-column">
      <h2>{title}</h2>
      <p className="muted small">
        기준: {result.criterion} · {result.elapsedMillis}ms · 근거로 쓰이는 조각 {relevantCount}개
      </p>
      {result.keywords && (
        <p className="small">
          뽑은 키워드:{' '}
          {result.keywords.length > 0 ? (
            result.keywords.map((keyword) => (
              <span key={keyword} className="keyword-chip">
                {keyword}
              </span>
            ))
          ) : (
            <span className="muted">없음</span>
          )}
        </p>
      )}
      {result.error && <p className="error">{result.error}</p>}
      {!result.error && result.results.length === 0 && (
        <p className="answer-not-found">찾은 조각 없음 → 문서에서 찾을 수 없음</p>
      )}
      {!result.error && result.results.length > 0 && relevantCount === 0 && (
        <p className="answer-not-found">기준을 넘은 조각 없음 → 문서에서 찾을 수 없음</p>
      )}
      <ol className="scored-list">
        {result.results.map((item) => (
          <li key={item.chunkId} className={item.relevant ? 'scored relevant' : 'scored'}>
            <div className="scored-head">
              <Link to={`/documents/${item.documentId}`}>
                {item.documentTitle} · {item.pageNo}쪽
              </Link>
              <span className="score">{item.score.toFixed(2)}</span>
            </div>
            <div className="score-bar">
              <span style={{ width: `${Math.max(0, Math.min(1, item.score)) * 100}%` }} />
            </div>
            <p className="small">
              {item.content.length > PREVIEW_LENGTH
                ? `${item.content.slice(0, PREVIEW_LENGTH)}…`
                : item.content}
            </p>
            <span className="muted small">{item.relevant ? '근거로 사용' : '기준 미달'}</span>
          </li>
        ))}
      </ol>
    </div>
  );
}
