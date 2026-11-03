# CLAUDE.md — AKAI

이 문서는 AI 코딩 도구가 이 저장소에서 작업할 때 따르는 지시 문서다. 시스템 설계서와 항상 일치하도록 유지하고, 설계가 바뀌면 이 문서도 같은 PR에서 함께 수정한다.

## 프로젝트 개요

AKAI는 기관 문서(PDF·TXT)를 업로드하면 요약·키워드를 생성하고, **업로드된 문서 내용만을 근거로** 질문에 답하는 웹 지식베이스다.

- 사용자: 이용자(문서 검색·요약 조회·질문·이력 조회), 관리자(이용자 기능 + 업로드·분류·태그·삭제)
- 계정은 미리 등록하며 회원가입은 없다.

## 기술 스택

- 백엔드: Java, Spring Boot, Spring Data JPA, H2(파일 모드), Apache PDFBox
- 프론트엔드: React, JavaScript, Vite
- 생성형 AI: OpenAI API

## 저장소 구조

- `backend/` Spring Boot 서버
- `frontend/` React 클라이언트
- `db/` 스키마 SQL·샘플 데이터
- `eval/` 평가 질문 세트·자동 채점 스크립트
- `docs/` 산출물 문서, 주간 진행 일지, AI 활용 로그

## 아키텍처

설계 근거와 다이어그램은 시스템 설계서(docs/)를 따른다.

- 서비스 전체: 클라이언트-서버 + 3계층 구조 (프레젠테이션 → 비즈니스 로직 → 데이터)
  - 브라우저(React)는 서버의 JSON API만 호출한다. 서버 쪽 View(MVC)는 만들지 않는다.
  - Controller는 요청·응답 변환과 권한 검사만 한다. 비즈니스 로직은 Service에 둔다.
  - Service는 Repository와 외부 연동 인터페이스에만 의존한다.
- 문서 처리: 데이터 흐름(파이프-필터)
  - 업로드 → 텍스트 추출(`TextExtractor`) → 조각 분할(`ChunkSplitter`) → 요약·키워드 생성(`SummaryGenerator`) → 저장
  - `DocumentPipeline`은 단계의 순서만 정하고, 각 단계는 독립된 클래스로 만든다.
- Q&A: `QaService`가 `SearchStrategy`로 조각을 찾고, 조각이 있을 때만 `AnswerGenerator`를 호출한다.

## 패키지 구조

```
backend/src/main/java/com/tadak/akai/
├── controller/   AuthController, DocumentController, QaController
├── service/      DocumentService, QaService, TagService
├── pipeline/     DocumentPipeline, ChunkSplitter
├── extractor/    TextExtractor, TextExtractorFactory, PdfTextExtractor, TxtTextExtractor
├── search/       SearchStrategy, KeywordSearchStrategy
├── ai/           SummaryGenerator, AnswerGenerator, OpenAiAdapter
├── domain/       User, Member, Admin, Document, Chunk, Summary, Category, Tag, Question, Answer
├── repository/   DocumentRepository, ChunkRepository, QuestionRepository, UserRepository, TagRepository
└── config/       SecurityConfig, AppConfig

frontend/src/
├── pages/        LoginPage, DocumentListPage, DocumentDetailPage, QaPage, HistoryPage, UploadPage, TagManagePage
├── components/   공통 컴포넌트(상단 메뉴, 근거 카드, AI 생성 배지)
└── api/          서버 API 호출 함수
```

- 상위 패키지는 extractor·search·ai의 **인터페이스**에만 의존한다. 구현 클래스를 직접 `new` 하지 않고 생성자 주입을 쓴다.

## 디자인 패턴 적용 지점

| 패턴 | 적용 위치 | 이유(무엇이 자주 바뀌는가) |
| --- | --- | --- |
| Factory Method | `TextExtractorFactory`가 파일 형식별 `TextExtractor`(`PdfTextExtractor`, `TxtTextExtractor`)를 만든다. 지원하지 않는 형식은 거부한다 | 지원 파일 형식이 추가된다 |
| Strategy | `QaService`는 `SearchStrategy`만 안다. MVP는 `KeywordSearchStrategy`, 확장 시 `EmbeddingSearchStrategy`를 설정으로 교체 | 검색 방식이 키워드에서 임베딩으로 바뀐다 |
| Adapter | `OpenAiAdapter`가 OpenAI API를 `SummaryGenerator`·`AnswerGenerator`로, `PdfTextExtractor`가 PDFBox를 `TextExtractor`로 감싼다 | 외부 라이브러리·AI 제공자가 바뀔 수 있다 |

## SOLID 규칙

- **SRP**: 한 클래스에 추출·분할·요약·저장을 몰지 않는다. 문서 처리 단계는 클래스를 나눈다.
- **OCP**: 새 파일 형식·검색 방식은 기존 클래스를 고치지 않고 인터페이스의 새 구현으로 추가한다.
- **LSP**: `TextExtractor` 구현은 모두 "페이지별 텍스트 목록"을 돌려준다(TXT는 전체를 1페이지로 본다).
- **ISP**: AI 연동 인터페이스는 용도별로 나눈다. `AiClient` 같은 통합 인터페이스를 만들지 않는다.
- **DIP**: Service는 OpenAI·PDFBox를 직접 호출하지 않고 인터페이스에 의존한다.

## 데이터베이스

H2 파일 모드, Spring Data JPA. 테이블은 시스템 설계서 ERD를 따른다.

- `users`(role: MEMBER/ADMIN, `Member`·`Admin` 상속을 단일 테이블로 저장), `categories`, `documents`(category_id, uploaded_by), `chunks`(document_id, page_no, seq, content), `summaries`(document_id 유일, keywords, ai_generated), `tags`, `document_tags`, `questions`(user_id), `answers`(question_id 유일, found), `answer_evidences`(answer_id, chunk_id)
- 문서를 삭제하면 조각·요약도 함께 삭제한다(구성). 태그·분류는 남긴다(집합).
- `answers.found`는 "문서에서 찾을 수 없음" 응답이면 false로 저장한다(거절률 측정용).
- 근거의 문서명·페이지는 `answer_evidences → chunks → documents`로 얻는다.

## 화면

로그인, 문서 목록·검색(홈), 문서 상세(요약·키워드), 질문하기, 근거 원문 보기(팝업), 질의 이력, [관리자] 문서 업로드, [관리자] 분류·태그 관리. 관리자 화면은 숨기되 권한 검사는 서버에서 한다.

## AI 기능 규칙 (반드시 지킬 것)

1. **답변은 검색된 조각만 근거로 생성한다.** 프롬프트에 검색된 조각 외의 내용을 넣지 않는다.
2. **관련 조각이 없으면 AI를 호출하지 않고** 고정 문구 `문서에서 찾을 수 없음`을 반환한다.
3. 모든 답변에 근거(문서명·페이지·원문)를 함께 반환·표시한다.
4. AI가 만든 요약·키워드·답변에는 화면에 **"AI 생성"** 표시를 붙인다.
5. 검색, 텍스트 추출, 조각 분할, 권한 검사는 AI가 아닌 코드가 수행한다.
6. AI 서비스가 응답하지 않으면 오류 안내를 표시하고 재시도를 제안한다.

## 보안 규칙

- `OPENAI_API_KEY`는 서버의 `.env` 파일로만 읽는다(`application.properties`에서 `spring.config.import`로 불러옴). 코드·프론트엔드에 키를 쓰지 않고, 키를 읽는 곳은 `OpenAiAdapter` 하나뿐이다.
- 키 이름만 적은 `.env.example`을 커밋해 팀원이 무엇을 넣을지 알게 한다.
- `.env`, 키가 들어간 파일은 절대 커밋하지 않는다.
- 업로드·삭제·분류·태그 수정 API는 관리자 권한을 서버에서 검사한다(프론트 숨김만으로 처리하지 않는다).

## 성능 목표

- 10페이지 PDF 처리(추출~요약 저장) 30초 이내
- 질문 응답 5초 이내
- 요약은 업로드 시 한 번만 생성해 저장하고, 조회 때 다시 생성하지 않는다.

## 코딩 규칙

> 팀 코딩 규칙 문서(WBS 3.1) 확정 후 이 절을 갱신한다. 아래는 초안이다.

**Java (ch08 코드 작성 규칙)**
- 클래스·인터페이스: PascalCase / 메서드·변수: camelCase / 상수: UPPER_SNAKE_CASE / 패키지: 소문자
- 들여쓰기 4칸(스페이스), 중괄호는 선언과 같은 줄에서 연다
- 한 줄 120자 이내, 메서드 하나는 한 가지 일만 한다(SRP)
- 이름만 보고 역할을 알 수 있게 짓고, 약어를 피한다

**JavaScript (React)**
- ESLint·Prettier 설정을 따른다
- 컴포넌트: PascalCase 파일·함수 / 훅: `use`로 시작 / 변수·함수: camelCase

## 작업 방식

- 한 번에 기능 하나(F-ID 단위)만 구현한다.
- 구현 전 관련 요구사항 ID와 설계서의 클래스·시퀀스 다이어그램을 확인한다.
- 기능 구현 시 정상·예외 테스트를 함께 작성한다.
- 커밋 메시지는 `[F-01] 내용` 형식으로 쓴다.
- 요구사항에 없는 기능을 임의로 추가하지 않는다.

## 테스트·실행 명령

> 프로젝트 골격 생성(11주차) 후 작성 예정
