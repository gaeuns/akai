-- AKAI DB 스키마 (시스템 설계서 8절 ERD, 테이블 10개) / H2 기준
-- 엔티티(backend/.../domain)와 enum 값·테이블·컬럼명을 맞춘 참조용 SQL이다.
-- 개발 중에는 JPA ddl-auto=update가 테이블을 만들므로, 이 파일은 DB를 비운 뒤 직접 실행할 때 쓴다.
-- 삭제 규칙: FK에는 ON DELETE를 두지 않는다.
--           구성 관계(문서→조각·요약, 질문→답변)의 삭제는 JPA cascade(cascade = ALL, orphanRemoval)가 처리하고,
--           집합 관계(분류·태그)는 서비스에서 처리한다.

-- 주의: 아래 DROP TABLE은 기존 테이블과 데이터를 모두 지운다. 개발 DB에 그대로 실행하면 데이터가 모두 삭제된다.

DROP TABLE IF EXISTS answer_evidences;
DROP TABLE IF EXISTS answers;
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS document_tags;
DROP TABLE IF EXISTS tags;
DROP TABLE IF EXISTS summaries;
DROP TABLE IF EXISTS chunks;
DROP TABLE IF EXISTS documents;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    login_id      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,              -- BCrypt
    name          VARCHAR(50)  NOT NULL,
    role          VARCHAR(10)  NOT NULL,              -- 상속(Member/Admin)을 단일 테이블+구분 컬럼으로 저장
    CONSTRAINT ck_users_role CHECK (role IN ('MEMBER', 'ADMIN'))
);

CREATE TABLE categories (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE tags (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE documents (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    file_name   VARCHAR(255) NOT NULL,
    file_type   VARCHAR(10)  NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'UPLOADED',     -- 파이프라인 진행 상태(DocumentStatus enum과 동일)
    category_id BIGINT,
    uploaded_by BIGINT,                                       -- 업로드한 관리자(로그인 F-08 구현 전에는 NULL 허용)
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_documents_file_type CHECK (file_type IN ('PDF', 'TXT')),
    CONSTRAINT ck_documents_status    CHECK (status IN ('UPLOADED', 'PROCESSING', 'READY', 'FAILED')),
    CONSTRAINT fk_documents_category  FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT fk_documents_uploader  FOREIGN KEY (uploaded_by) REFERENCES users (id)
);

CREATE TABLE chunks (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT  NOT NULL,
    page_no     INT     NOT NULL,                             -- TXT는 전부 1페이지
    seq         INT     NOT NULL,                             -- 문서 안에서의 조각 순서
    content     VARCHAR(10000) NOT NULL,                      -- CLOB은 upper() 검색이 안 되어 VARCHAR 사용
    CONSTRAINT uq_chunks_doc_seq UNIQUE (document_id, seq),
    CONSTRAINT ck_chunks_page    CHECK (page_no >= 1),
    CONSTRAINT fk_chunks_document FOREIGN KEY (document_id) REFERENCES documents (id)
);

CREATE TABLE summaries (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id  BIGINT       NOT NULL UNIQUE,                -- 문서와 1:1
    content      TEXT         NOT NULL,
    keywords     VARCHAR(500),                                -- 쉼표로 구분
    ai_generated BOOLEAN      NOT NULL DEFAULT TRUE,          -- 관리자가 수정하면 FALSE
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_summaries_document FOREIGN KEY (document_id) REFERENCES documents (id)
);

CREATE TABLE document_tags (
    document_id BIGINT NOT NULL,
    tag_id      BIGINT NOT NULL,
    PRIMARY KEY (document_id, tag_id),
    CONSTRAINT fk_dt_document FOREIGN KEY (document_id) REFERENCES documents (id),
    CONSTRAINT fk_dt_tag      FOREIGN KEY (tag_id)      REFERENCES tags (id)
);

CREATE TABLE questions (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id  BIGINT    NOT NULL,
    content  TEXT      NOT NULL,
    asked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_questions_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE answers (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT  NOT NULL UNIQUE,                      -- 질문과 1:1
    content     TEXT    NOT NULL,
    found       BOOLEAN NOT NULL,                             -- FALSE = "문서에서 찾을 수 없음" (거절률 측정)
    CONSTRAINT fk_answers_question FOREIGN KEY (question_id) REFERENCES questions (id)
);

CREATE TABLE answer_evidences (
    answer_id BIGINT NOT NULL,
    chunk_id  BIGINT NOT NULL,
    PRIMARY KEY (answer_id, chunk_id),
    CONSTRAINT fk_ae_answer FOREIGN KEY (answer_id) REFERENCES answers (id),
    CONSTRAINT fk_ae_chunk  FOREIGN KEY (chunk_id)  REFERENCES chunks (id)
);

CREATE INDEX idx_documents_category ON documents (category_id);
CREATE INDEX idx_documents_uploader ON documents (uploaded_by);
CREATE INDEX idx_questions_user     ON questions (user_id, asked_at);
CREATE INDEX idx_dt_tag             ON document_tags (tag_id);
CREATE INDEX idx_ae_chunk           ON answer_evidences (chunk_id);
