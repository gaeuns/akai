# eval

답변 정확도 평가 자료를 둔다 (NF-01 정답률·근거 표시율·거절률, NF-02 응답 시간).

| 파일 | 내용 | WBS |
| --- | --- | --- |
| `questions.csv` | 평가 질문 24개(답 있음 17, 답 없음 7)와 정답 핵심어·근거 문서·쪽 | 4.4 |
| `questions_paraphrase.csv` | 같은 정답을 문서와 다른 말로 물은 보조 질문 7개 (F-06 임베딩 검색 확인용, NF-01 지표에는 넣지 않음) | 4.4 |
| `evaluate.py` | 자동 채점 스크립트 (표준 라이브러리만 사용) | 5.2 |
| `results/` | 측정 원자료 CSV (`날짜시각-label.csv`) | 5.2 |

측정 결과와 분석은 `docs/AI_활용_검증_보고서.md`에 정리한다.

## 질문 파일 형식

UTF-8 CSV. 칸: `번호, 질문, 유형, 정답 핵심어, 근거 문서, 근거 쪽`

- 유형: `답 있음` 또는 `답 없음`(문서에 없는 질문, 거절률 측정용)
- 정답 핵심어: 답변에 들어 있어야 하는 말. `;`는 모두 포함, `|`는 그중 하나 (예: `4분의3|75%`, `5회;30분`). 공백·쉼표는 무시하고 비교한다
- 근거 문서: 업로드했을 때의 문서 제목(파일 이름에서 확장자를 뺀 것), 근거 쪽: PDF 쪽 번호(TXT는 1)
- 정답과 쪽은 `docs/01~05` 원문에서 확인해 적는다. 답 없음 질문은 비워 둔다

## 채점 기준

- 정답률 = 답 있음 질문 중 found=true이고 정답 핵심어가 모두 들어 있는 비율
- 근거 표시율 = found=true인 답변 중 근거가 1개 이상 온 비율
- 거절률 = 답 없음 질문 중 found=false("문서에서 찾을 수 없음")인 비율
- 참고: 근거 일치율(정답 근거 문서·쪽이 근거에 있는 비율), 평균·최대 응답 시간, 5초 이내 비율

## 실행

시연 문서 5개를 올린 서버를 켠 상태에서 저장소 루트에서 실행한다. 정확도 평가는 샘플 데이터 없이 시연 문서만 올린 DB에서 한다.

```bash
python eval/evaluate.py --check                    # 채점 규칙 자체 확인 (서버 불필요)
python eval/evaluate.py --label embedding          # 기본: member01로 http://localhost:8080 에 질문
python eval/evaluate.py --questions questions_paraphrase.csv --label embedding-paraphrase
```

검색 방식을 바꿔 비교하려면 서버를 `.\gradlew.bat bootRun --args=--app.search.strategy=keyword`로 다시 켜고 `--label keyword`로 실행한다. 질문은 질의 이력과 품질 지표(F-09)에 남는다.
