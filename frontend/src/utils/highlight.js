// F-11: 근거 원문에서 질문과 관련된 말을 찾기 위한 단어 뽑기.
// 서버 키워드 검색(KeywordSearchStrategy)과 같은 방식으로 조사·어미를 떼고 불용어를 뺀다.
const SUFFIXES = [
  '으로부터',
  '에서부터',
  '하려면',
  '있나요',
  '없나요',
  '되나요',
  '하나요',
  '한가요',
  '인가요',
  '합니까',
  '됩니까',
  '입니까',
  '습니까',
  '에서는',
  '으로는',
  '까지는',
  '이란',
  '이라',
  '에서',
  '에게',
  '까지',
  '부터',
  '으로',
  '처럼',
  '보다',
  '이나',
  '하고',
  '하면',
  '하는',
  '되는',
  '해야',
  '나요',
  '가요',
  '은',
  '는',
  '이',
  '가',
  '을',
  '를',
  '에',
  '의',
  '로',
  '와',
  '과',
  '도',
  '만',
  '요',
  '한',
  '할',
  '된',
];

const STOP_WORDS = new Set([
  '무엇',
  '뭐야',
  '어떻게',
  '어떤',
  '언제',
  '어디',
  '누구',
  '얼마',
  '얼마나',
  '알려',
  '알려줘',
  '설명',
  '대해',
  '대한',
  '관련',
  '경우',
  '가능',
  '가능한',
  '방법',
  '주세요',
  '있는',
  '없는',
  '있나',
  '있을까',
  '하나',
  '할까',
  '되나',
  '받을',
  '받는',
  '궁금',
]);

// 질문과 문서가 다르게 쓰는 말 (서버 동의어와 같다)
const SYNONYMS = { 퍼센트: ['%', '비율'], 프로: ['%'], 학점: ['등급'] };

function stem(word) {
  let result = word.startsWith('몇') && word.length >= 3 ? word.slice(1) : word;
  const suffix = SUFFIXES.find((s) => result.endsWith(s) && result.length - s.length >= 2);
  if (suffix) {
    result = result.slice(0, result.length - suffix.length);
  }
  return result;
}

export function extractTerms(question) {
  if (!question) {
    return [];
  }
  const terms = new Set();
  question
    .toLowerCase()
    .split(/[^\p{L}\p{N}]+/u)
    .filter((word) => word && !STOP_WORDS.has(word))
    .map(stem)
    .filter((word) => (word.length >= 2 || /^[a-z]$/.test(word)) && !STOP_WORDS.has(word))
    .forEach((word) => {
      terms.add(word);
      (SYNONYMS[word] ?? []).forEach((synonym) => terms.add(synonym));
    });
  // 긴 말부터 찾는다. 한 글자 영문(학점 등급 "A")은 단어 경계가 있을 때만 표시한다
  return [...terms].sort((a, b) => b.length - a.length);
}

function escapeRegExp(text) {
  return text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

// 텍스트를 [{ text, match }] 조각으로 나눈다
export function splitByTerms(text, terms) {
  if (!text || terms.length === 0) {
    return [{ text: text ?? '', match: false }];
  }
  const pattern = terms
    .map((term) => (/^[a-z]$/.test(term) ? `\\b${term}\\b` : escapeRegExp(term)))
    .join('|');
  const splitter = new RegExp(`(${pattern})`, 'giu');
  const matcher = new RegExp(`^(?:${pattern})$`, 'iu');
  return text
    .split(splitter)
    .filter((part) => part !== '')
    .map((part) => ({ text: part, match: matcher.test(part) }));
}
