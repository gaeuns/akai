-- AKAI 샘플 데이터 (전부 가상 데이터, 실제 개인정보·기관 문서 아님)
-- 로그인: 모든 계정 비밀번호 akai1234 (BCrypt)
-- 실행 순서: schema.sql -> sample-data.sql
--
-- 주의: 개발·화면 확인용이다. 정확도 평가는 샘플 없이 시연 문서만 올린 DB로 진행한다.
--       (샘플의 가상대학교 문서와 시연 문서는 같은 주제에서 수치가 달라 Q&A 근거와 정답률 측정이 섞인다.)

-- users
INSERT INTO users (id, login_id, password_hash, name, role) VALUES (1, 'admin01', '$2a$10$HLNTI4P.eBM6Rp4wrqKI5.xH7MlG7y0gB.eTLKfP0DGfRZ2FDLEPq', '김관리', 'ADMIN');
INSERT INTO users (id, login_id, password_hash, name, role) VALUES (2, 'admin02', '$2a$10$HLNTI4P.eBM6Rp4wrqKI5.xH7MlG7y0gB.eTLKfP0DGfRZ2FDLEPq', '이문서', 'ADMIN');
INSERT INTO users (id, login_id, password_hash, name, role) VALUES (3, 'member01', '$2a$10$HLNTI4P.eBM6Rp4wrqKI5.xH7MlG7y0gB.eTLKfP0DGfRZ2FDLEPq', '박학생', 'MEMBER');
INSERT INTO users (id, login_id, password_hash, name, role) VALUES (4, 'member02', '$2a$10$HLNTI4P.eBM6Rp4wrqKI5.xH7MlG7y0gB.eTLKfP0DGfRZ2FDLEPq', '최연구', 'MEMBER');
INSERT INTO users (id, login_id, password_hash, name, role) VALUES (5, 'member03', '$2a$10$HLNTI4P.eBM6Rp4wrqKI5.xH7MlG7y0gB.eTLKfP0DGfRZ2FDLEPq', '정교직', 'MEMBER');
INSERT INTO users (id, login_id, password_hash, name, role) VALUES (6, 'member04', '$2a$10$HLNTI4P.eBM6Rp4wrqKI5.xH7MlG7y0gB.eTLKfP0DGfRZ2FDLEPq', '한민원', 'MEMBER');

-- categories
INSERT INTO categories (id, name) VALUES (1, '규정·지침');
INSERT INTO categories (id, name) VALUES (2, '업무 매뉴얼');
INSERT INTO categories (id, name) VALUES (3, '강의자료');
INSERT INTO categories (id, name) VALUES (4, '이용 안내문');

-- tags
INSERT INTO tags (id, name) VALUES (1, '학사');
INSERT INTO tags (id, name) VALUES (2, '수강신청');
INSERT INTO tags (id, name) VALUES (3, '성적');
INSERT INTO tags (id, name) VALUES (4, '졸업');
INSERT INTO tags (id, name) VALUES (5, '휴학');
INSERT INTO tags (id, name) VALUES (6, '연구윤리');
INSERT INTO tags (id, name) VALUES (7, '연구실안전');
INSERT INTO tags (id, name) VALUES (8, '소프트웨어공학');
INSERT INTO tags (id, name) VALUES (9, '아키텍처');
INSERT INTO tags (id, name) VALUES (10, '정보공개');
INSERT INTO tags (id, name) VALUES (11, '도서관');
INSERT INTO tags (id, name) VALUES (12, '대출');

-- documents
INSERT INTO documents (id, title, file_name, file_type, status, category_id, uploaded_by, created_at) VALUES (1, '가상대학교 학사운영규정', 'academic_regulation.pdf', 'PDF', 'READY', 1, 1, '2026-10-27 10:05:12');
INSERT INTO documents (id, title, file_name, file_type, status, category_id, uploaded_by, created_at) VALUES (2, '가상대학교 연구윤리 지침', 'research_ethics.pdf', 'PDF', 'READY', 1, 1, '2026-10-27 10:20:41');
INSERT INTO documents (id, title, file_name, file_type, status, category_id, uploaded_by, created_at) VALUES (3, '연구실 안전관리 매뉴얼', 'lab_safety_manual.pdf', 'PDF', 'READY', 2, 2, '2026-10-28 14:02:33');
INSERT INTO documents (id, title, file_name, file_type, status, category_id, uploaded_by, created_at) VALUES (4, '소프트웨어공학 3주차 - 소프트웨어 아키텍처', 'swe_week3_architecture.txt', 'TXT', 'READY', 3, 2, '2026-10-29 09:15:08');
INSERT INTO documents (id, title, file_name, file_type, status, category_id, uploaded_by, created_at) VALUES (5, '가상시청 정보공개 업무 처리 매뉴얼', 'info_disclosure_manual.pdf', 'PDF', 'READY', 2, 1, '2026-10-30 11:48:19');
INSERT INTO documents (id, title, file_name, file_type, status, category_id, uploaded_by, created_at) VALUES (6, '중앙도서관 이용 안내', 'library_guide.txt', 'TXT', 'READY', 4, 2, '2026-11-02 16:30:05');

-- document_tags
INSERT INTO document_tags (document_id, tag_id) VALUES (1, 1);
INSERT INTO document_tags (document_id, tag_id) VALUES (1, 2);
INSERT INTO document_tags (document_id, tag_id) VALUES (1, 3);
INSERT INTO document_tags (document_id, tag_id) VALUES (1, 4);
INSERT INTO document_tags (document_id, tag_id) VALUES (1, 5);
INSERT INTO document_tags (document_id, tag_id) VALUES (2, 6);
INSERT INTO document_tags (document_id, tag_id) VALUES (3, 7);
INSERT INTO document_tags (document_id, tag_id) VALUES (4, 8);
INSERT INTO document_tags (document_id, tag_id) VALUES (4, 9);
INSERT INTO document_tags (document_id, tag_id) VALUES (5, 10);
INSERT INTO document_tags (document_id, tag_id) VALUES (6, 11);
INSERT INTO document_tags (document_id, tag_id) VALUES (6, 12);

-- summaries (문서 6은 관리자가 직접 수정한 경우라 ai_generated=FALSE)
INSERT INTO summaries (id, document_id, content, keywords, ai_generated, updated_at) VALUES (1, 1, '가상대학교 학사운영규정은 학년도·학기 구성, 수강신청·정정·철회, 출석과 성적평가, 졸업 요건, 휴학·복학, 성적 이의신청, 학사경고 기준을 규정한다. 학기당 신청 학점은 최대 18학점이며 졸업에는 130학점이 필요하다.', '학사운영, 수강신청, 수강철회, 출석, 성적평가, 졸업학점, 휴학, 학사경고', TRUE, '2026-10-27 10:05:12');
INSERT INTO summaries (id, document_id, content, keywords, ai_generated, updated_at) VALUES (2, 2, '연구윤리 지침은 위조·변조·표절·부당한 저자 표시·부당한 중복 게재를 연구 부정행위로 정의하고, 제보 접수 후 예비조사 30일, 본조사 90일 이내 절차와 제보자 보호 원칙을 규정한다.', '연구윤리, 연구부정행위, 위조, 변조, 표절, 예비조사, 본조사, 제보자보호', TRUE, '2026-10-27 10:20:41');
INSERT INTO summaries (id, document_id, content, keywords, ai_generated, updated_at) VALUES (3, 3, '연구실 안전관리 매뉴얼은 신규 구성원 8시간 이상 안전교육, 인화성·반응성 화학물질의 분리 보관과 MSDS 비치, 사고 발생 시 즉시 보고 및 중대사고 24시간 이내 기관장 보고 절차를 안내한다.', '연구실안전, 안전교육, 화학물질, 인화성물질, MSDS, 사고보고', TRUE, '2026-10-28 14:02:33');
INSERT INTO summaries (id, document_id, content, keywords, ai_generated, updated_at) VALUES (4, 4, '소프트웨어 아키텍처의 정의와 함께 계층 구조, 클라이언트-서버, 파이프-필터 스타일의 특징과 장단점을 정리한 강의자료이다.', '소프트웨어아키텍처, 계층구조, 클라이언트서버, 파이프필터, 품질속성', TRUE, '2026-10-29 09:15:08');
INSERT INTO summaries (id, document_id, content, keywords, ai_generated, updated_at) VALUES (5, 5, '정보공개 청구의 접수부터 공개 여부 결정(10일 이내, 부득이한 경우 10일 연장), 비공개 대상, 공개 방법과 수수료, 이의신청(30일 이내) 절차를 단계별로 안내한다.', '정보공개, 청구, 공개결정, 비공개, 수수료, 이의신청', TRUE, '2026-10-30 11:48:19');
INSERT INTO summaries (id, document_id, content, keywords, ai_generated, updated_at) VALUES (6, 6, '중앙도서관 개관 시간, 학부생·대학원생별 대출 권수와 기간, 연장 방법, 연체 시 대출 정지 기준을 안내한다.', '도서관, 대출, 연장, 연체, 개관시간', FALSE, '2026-11-02 16:30:05');

-- chunks
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (1, 1, 1, 1, '제1조(목적) 이 규정은 가상대학교(이하 "본교")의 학사 운영에 필요한 사항을 정함을 목적으로 한다. 제2조(학년도와 학기) 학년도는 3월 1일에 시작하여 다음 해 2월 말일에 끝나며, 1학기(3월 1일~8월 31일)와 2학기(9월 1일~2월 말일)로 나눈다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (2, 1, 2, 2, '제5조(수강신청) 학생은 매 학기 개강 전 수강신청 기간에 수강신청을 하여야 한다. 학기당 신청할 수 있는 학점은 최대 18학점이다. 다만 직전 학기 평점평균이 4.0 이상인 학생은 3학점 범위에서 추가로 신청할 수 있다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (3, 1, 3, 3, '제8조(수강정정) 수강정정은 개강 후 1주 이내에 할 수 있다. 제9조(수강철회) 수강철회는 개강 후 9주차 종료일까지 신청할 수 있으며, 철회한 과목은 성적표에 "W"로 표기한다. 수강철회 후에도 학기 최소 신청 학점인 9학점 이상을 유지하여야 한다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (4, 1, 4, 4, '제12조(출석) 학생은 해당 과목 수업 시간의 3분의 2 이상 출석하여야 성적을 받을 수 있다. 출석이 수업 시간의 3분의 2에 미달하면 해당 과목은 F로 처리한다. 질병, 경조사, 공식 행사 참여로 인한 결석은 증빙서류를 제출하면 공결로 인정한다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (5, 1, 5, 5, '제15조(성적평가) 성적은 A+, A0, B+, B0, C+, C0, D+, D0, F의 9단계로 평가하며 상대평가를 원칙으로 한다. A+와 A0를 합한 A등급은 수강 인원의 30퍼센트 이내로 부여한다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (6, 1, 6, 6, '제18조(졸업) 졸업에 필요한 학점은 130학점이며, 이 중 전공 학점이 66학점 이상이어야 한다. 졸업 시 전체 평점평균은 2.0 이상이어야 한다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (7, 1, 7, 7, '제21조(휴학) 일반휴학은 학기 개시일 전까지 신청하여야 한다. 휴학 기간은 통산 6학기를 초과할 수 없다. 다만 군 복무를 위한 휴학 기간은 통산 휴학 기간에 산입하지 않는다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (8, 1, 8, 8, '제24조(복학) 휴학 기간이 끝난 학생은 복학 신청 기간에 복학을 신청하여야 한다. 복학 신청을 하지 않으면 제적 대상이 될 수 있다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (9, 1, 9, 9, '제27조(성적 이의신청) 학생은 성적 공시일부터 7일 이내에 담당 교수에게 성적 이의신청을 할 수 있다. 담당 교수는 이의신청을 받은 날부터 5일 이내에 처리 결과를 학생에게 통보하여야 한다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (10, 1, 10, 10, '제30조(학사경고) 학기 평점평균이 1.75 미만인 학생에게는 학사경고를 한다. 학사경고를 연속 3회 받은 학생은 제적 대상이 된다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (11, 2, 1, 1, '제1조(목적) 이 지침은 연구 부정행위를 예방하고 연구의 진실성을 확보함을 목적으로 한다. 제3조(연구 부정행위) 연구 부정행위란 위조, 변조, 표절, 부당한 저자 표시, 부당한 중복 게재를 말한다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (12, 2, 2, 2, '위조는 존재하지 않는 데이터 또는 연구 결과를 허위로 만들어 내는 행위를 말한다. 변조는 연구 재료, 장비, 과정을 인위적으로 조작하거나 데이터를 임의로 변경 또는 삭제하여 연구 내용을 왜곡하는 행위를 말한다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (13, 2, 3, 3, '제7조(제보와 조사) 연구 부정행위 제보는 연구윤리위원회에 서면 또는 전자우편으로 한다. 위원회는 제보를 접수한 날부터 30일 이내에 예비조사를 마쳐야 하며, 본조사는 착수한 날부터 90일 이내에 종료하여야 한다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (14, 2, 4, 4, '제9조(제보자 보호) 위원회는 제보자의 신원을 비밀로 하여야 하며, 제보를 이유로 제보자에게 불이익을 주어서는 안 된다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (15, 3, 1, 1, '신규 연구실 구성원은 연구활동을 시작하기 전에 안전교육을 8시간 이상 받아야 한다. 이후 정기 안전교육은 연 6시간 이상 이수하여야 한다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (16, 3, 2, 2, '인화성 물질은 인화성 물질 전용 보관함에 보관한다. 서로 반응할 수 있는 화학물질은 분리하여 보관하고, 모든 화학물질에는 물질안전보건자료(MSDS)를 연구실 내에 비치하여야 한다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (17, 3, 3, 3, '사고가 발생하면 즉시 연구실 안전관리 담당자에게 보고한다. 인명 피해가 있는 중대사고는 사고 발생 후 24시간 이내에 기관장에게 보고하여야 한다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (18, 4, 1, 1, '소프트웨어 아키텍처는 시스템을 구성하는 요소와 요소 사이의 관계, 그리고 이들을 지배하는 설계 원칙을 말한다. 아키텍처는 성능, 보안성, 변경 용이성 같은 품질 속성에 큰 영향을 준다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (19, 4, 1, 2, '계층 구조는 시스템을 프레젠테이션, 비즈니스 로직, 데이터 접근 계층으로 나누어 각 계층이 바로 아래 계층만 사용하게 한다. 관심사가 분리되어 테스트와 교체가 쉬운 반면, 단순한 요청도 여러 계층을 거쳐 성능 오버헤드가 생길 수 있다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (20, 4, 1, 3, '클라이언트-서버 구조는 서비스를 요청하는 클라이언트와 제공하는 서버로 역할을 나눈다. 데이터와 핵심 로직을 서버에서 관리할 수 있어 보안과 유지보수에 유리하지만, 서버가 다운되면 전체 서비스가 중단될 수 있다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (21, 4, 1, 4, '파이프-필터 구조는 처리 과정을 독립된 필터로 나누고 필터를 파이프로 연결해 데이터를 순서대로 전달한다. 각 단계를 독립적으로 교체하고 테스트할 수 있다는 장점이 있으나, 사용자와 주고받는 대화형 시스템에는 적합하지 않다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (22, 5, 1, 1, '정보공개 청구서가 접수되면 담당 부서는 접수한 날부터 10일 이내에 공개 여부를 결정하여 청구인에게 통지하여야 한다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (23, 5, 2, 2, '부득이한 사유가 있는 경우에는 결정 기간을 10일 범위에서 연장할 수 있다. 기간을 연장하면 연장 사유와 연장된 기한을 청구인에게 지체 없이 통지하여야 한다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (24, 5, 3, 3, '개인의 주민등록번호 등 개인정보가 포함된 정보와 진행 중인 감사·조사에 관한 정보는 비공개 대상이 될 수 있다. 비공개로 결정하는 경우에는 그 이유를 서면으로 알려야 한다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (25, 5, 4, 4, '정보는 열람 또는 사본 교부의 방법으로 공개한다. 열람은 수수료가 없으며, 사본은 1장당 50원의 수수료를 받는다. 전자파일로 공개하는 경우에는 수수료를 받지 않는다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (26, 5, 5, 5, '청구인은 공개 결정 통지를 받은 날부터 30일 이내에 담당 부서에 이의신청을 할 수 있다. 담당 부서는 이의신청을 받은 날부터 7일 이내에 결과를 통지하여야 한다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (27, 6, 1, 1, '중앙도서관은 평일 09:00부터 22:00까지, 토요일은 09:00부터 17:00까지 운영하며 일요일과 공휴일은 휴관한다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (28, 6, 1, 2, '대출 권수와 기간은 학부생 5권 14일, 대학원생 10권 30일이다. 대출 기간은 연체 도서가 없는 경우 1회에 한하여 7일 연장할 수 있다.');
INSERT INTO chunks (id, document_id, page_no, seq, content) VALUES (29, 6, 1, 3, '반납 기한을 넘기면 연체일수만큼 도서 대출이 정지된다. 예를 들어 3일 연체한 경우 반납한 날부터 3일간 새로운 대출을 할 수 없다.');

-- questions / answers / answer_evidences
INSERT INTO questions (id, user_id, content, asked_at) VALUES (1, 3, '학기당 신청할 수 있는 최대 학점과 수강철회 후 유지해야 하는 최소 학점은 얼마인가요?', '2026-11-02 10:11:20');
INSERT INTO answers (id, question_id, content, found) VALUES (1, 1, '학기당 신청 가능한 학점은 최대 18학점이며, 수강철회 후에도 학기 최소 신청 학점인 9학점 이상을 유지해야 합니다.', TRUE);
INSERT INTO answer_evidences (answer_id, chunk_id) VALUES (1, 2);
INSERT INTO answer_evidences (answer_id, chunk_id) VALUES (1, 3);
INSERT INTO questions (id, user_id, content, asked_at) VALUES (2, 3, '수강철회는 언제까지 신청할 수 있나요?', '2026-11-02 10:13:02');
INSERT INTO answers (id, question_id, content, found) VALUES (2, 2, '개강 후 9주차 종료일까지 신청할 수 있으며, 철회한 과목은 성적표에 "W"로 표기됩니다.', TRUE);
INSERT INTO answer_evidences (answer_id, chunk_id) VALUES (2, 3);
INSERT INTO questions (id, user_id, content, asked_at) VALUES (3, 3, '성적을 받으려면 출석을 얼마나 해야 하나요?', '2026-11-02 10:15:47');
INSERT INTO answers (id, question_id, content, found) VALUES (3, 3, '해당 과목 수업 시간의 3분의 2 이상 출석해야 하며, 미달하면 해당 과목은 F로 처리됩니다.', TRUE);
INSERT INTO answer_evidences (answer_id, chunk_id) VALUES (3, 4);
INSERT INTO questions (id, user_id, content, asked_at) VALUES (4, 3, 'A등급은 한 과목에서 몇 퍼센트까지 줄 수 있나요?', '2026-11-02 10:18:30');
INSERT INTO answers (id, question_id, content, found) VALUES (4, 4, 'A+와 A0를 합한 A등급은 수강 인원의 30퍼센트 이내로 부여합니다.', TRUE);
INSERT INTO answer_evidences (answer_id, chunk_id) VALUES (4, 5);
INSERT INTO questions (id, user_id, content, asked_at) VALUES (5, 3, '졸업하려면 몇 학점이 필요한가요?', '2026-11-02 10:20:11');
INSERT INTO answers (id, question_id, content, found) VALUES (5, 5, '졸업에는 130학점이 필요하고 그중 전공 학점이 66학점 이상이어야 하며, 전체 평점평균은 2.0 이상이어야 합니다.', TRUE);
INSERT INTO answer_evidences (answer_id, chunk_id) VALUES (5, 6);
INSERT INTO questions (id, user_id, content, asked_at) VALUES (6, 5, '일반휴학은 최대 몇 학기까지 가능한가요?', '2026-11-03 13:02:44');
INSERT INTO answers (id, question_id, content, found) VALUES (6, 6, '휴학 기간은 통산 6학기를 초과할 수 없습니다. 다만 군 복무를 위한 휴학 기간은 통산 휴학 기간에 산입되지 않습니다.', TRUE);
INSERT INTO answer_evidences (answer_id, chunk_id) VALUES (6, 7);
INSERT INTO questions (id, user_id, content, asked_at) VALUES (7, 3, '성적 이의신청은 언제까지 할 수 있나요?', '2026-11-03 14:20:09');
INSERT INTO answers (id, question_id, content, found) VALUES (7, 7, '성적 공시일부터 7일 이내에 담당 교수에게 신청할 수 있습니다. 담당 교수는 접수일부터 5일 이내에 결과를 통보해야 합니다.', TRUE);
INSERT INTO answer_evidences (answer_id, chunk_id) VALUES (7, 9);
INSERT INTO questions (id, user_id, content, asked_at) VALUES (8, 3, '학사경고는 어떤 경우에 받나요?', '2026-11-03 14:22:31');
INSERT INTO answers (id, question_id, content, found) VALUES (8, 8, '학기 평점평균이 1.75 미만이면 학사경고를 받으며, 연속 3회 받으면 제적 대상이 됩니다.', TRUE);
INSERT INTO answer_evidences (answer_id, chunk_id) VALUES (8, 10);
INSERT INTO questions (id, user_id, content, asked_at) VALUES (9, 4, '연구 부정행위에는 어떤 것들이 있나요?', '2026-11-04 09:30:12');
INSERT INTO answers (id, question_id, content, found) VALUES (9, 9, '위조, 변조, 표절, 부당한 저자 표시, 부당한 중복 게재가 연구 부정행위에 해당합니다.', TRUE);
INSERT INTO answer_evidences (answer_id, chunk_id) VALUES (9, 11);
INSERT INTO questions (id, user_id, content, asked_at) VALUES (10, 4, '연구 부정행위 본조사는 얼마 안에 끝내야 하나요?', '2026-11-04 09:33:58');
INSERT INTO answers (id, question_id, content, found) VALUES (10, 10, '본조사는 착수한 날부터 90일 이내에 종료해야 합니다. 예비조사는 제보 접수일부터 30일 이내에 마쳐야 합니다.', TRUE);
INSERT INTO answer_evidences (answer_id, chunk_id) VALUES (10, 13);
INSERT INTO questions (id, user_id, content, asked_at) VALUES (11, 4, '신규 연구실 구성원의 안전교육은 몇 시간인가요?', '2026-11-04 10:05:20');
INSERT INTO answers (id, question_id, content, found) VALUES (11, 11, '연구활동을 시작하기 전에 안전교육을 8시간 이상 받아야 하며, 이후 정기교육은 연 6시간 이상 이수해야 합니다.', TRUE);
INSERT INTO answer_evidences (answer_id, chunk_id) VALUES (11, 15);
INSERT INTO questions (id, user_id, content, asked_at) VALUES (12, 4, '인화성 물질은 어떻게 보관해야 하나요?', '2026-11-04 10:07:41');
INSERT INTO answers (id, question_id, content, found) VALUES (12, 12, '인화성 물질 전용 보관함에 보관하고, 서로 반응할 수 있는 화학물질은 분리하여 보관해야 합니다. 모든 화학물질의 MSDS는 연구실 내에 비치해야 합니다.', TRUE);
INSERT INTO answer_evidences (answer_id, chunk_id) VALUES (12, 16);
INSERT INTO questions (id, user_id, content, asked_at) VALUES (13, 3, '파이프-필터 구조의 장점은 무엇인가요?', '2026-11-05 15:11:09');
INSERT INTO answers (id, question_id, content, found) VALUES (13, 13, '처리 과정을 독립된 필터로 나누기 때문에 각 단계를 독립적으로 교체하고 테스트할 수 있습니다. 다만 대화형 시스템에는 적합하지 않습니다.', TRUE);
INSERT INTO answer_evidences (answer_id, chunk_id) VALUES (13, 21);
INSERT INTO questions (id, user_id, content, asked_at) VALUES (14, 6, '정보공개 청구를 받으면 며칠 안에 공개 여부를 결정해야 하나요?', '2026-11-06 11:00:15');
INSERT INTO answers (id, question_id, content, found) VALUES (14, 14, '청구서를 접수한 날부터 10일 이내에 공개 여부를 결정하여 청구인에게 통지해야 합니다.', TRUE);
INSERT INTO answer_evidences (answer_id, chunk_id) VALUES (14, 22);
INSERT INTO questions (id, user_id, content, asked_at) VALUES (15, 6, '공개 여부 결정 기간을 연장할 수 있나요?', '2026-11-06 11:02:37');
INSERT INTO answers (id, question_id, content, found) VALUES (15, 15, '부득이한 사유가 있으면 10일 범위에서 연장할 수 있으며, 연장 사유와 기한을 청구인에게 지체 없이 통지해야 합니다.', TRUE);
INSERT INTO answer_evidences (answer_id, chunk_id) VALUES (15, 23);
INSERT INTO questions (id, user_id, content, asked_at) VALUES (16, 6, '정보공개 이의신청 기한은 언제까지인가요?', '2026-11-06 11:05:50');
INSERT INTO answers (id, question_id, content, found) VALUES (16, 16, '공개 결정 통지를 받은 날부터 30일 이내에 담당 부서에 이의신청을 할 수 있습니다.', TRUE);
INSERT INTO answer_evidences (answer_id, chunk_id) VALUES (16, 26);
INSERT INTO questions (id, user_id, content, asked_at) VALUES (17, 5, '학부생은 도서를 몇 권, 며칠 빌릴 수 있나요?', '2026-11-09 09:12:33');
INSERT INTO answers (id, question_id, content, found) VALUES (17, 17, '학부생은 5권을 14일간 대출할 수 있으며, 연체 도서가 없으면 1회에 한해 7일 연장할 수 있습니다.', TRUE);
INSERT INTO answer_evidences (answer_id, chunk_id) VALUES (17, 28);
INSERT INTO questions (id, user_id, content, asked_at) VALUES (18, 5, '도서를 연체하면 어떻게 되나요?', '2026-11-09 09:14:02');
INSERT INTO answers (id, question_id, content, found) VALUES (18, 18, '연체일수만큼 도서 대출이 정지됩니다. 예를 들어 3일 연체하면 반납한 날부터 3일간 새로운 대출을 할 수 없습니다.', TRUE);
INSERT INTO answer_evidences (answer_id, chunk_id) VALUES (18, 29);
INSERT INTO questions (id, user_id, content, asked_at) VALUES (19, 3, '기숙사 입사 신청은 언제 하나요?', '2026-11-09 10:20:41');
INSERT INTO answers (id, question_id, content, found) VALUES (19, 19, '문서에서 찾을 수 없음', FALSE);
INSERT INTO questions (id, user_id, content, asked_at) VALUES (20, 3, '성적우수 장학금 신청 자격은 어떻게 되나요?', '2026-11-09 10:22:18');
INSERT INTO answers (id, question_id, content, found) VALUES (20, 20, '문서에서 찾을 수 없음', FALSE);
INSERT INTO questions (id, user_id, content, asked_at) VALUES (21, 4, '연구비 정산은 어떻게 하나요?', '2026-11-09 11:01:55');
INSERT INTO answers (id, question_id, content, found) VALUES (21, 21, '문서에서 찾을 수 없음', FALSE);
INSERT INTO questions (id, user_id, content, asked_at) VALUES (22, 5, '대출한 도서를 분실하면 어떻게 배상하나요?', '2026-11-09 11:30:27');
INSERT INTO answers (id, question_id, content, found) VALUES (22, 22, '문서에서 찾을 수 없음', FALSE);
INSERT INTO questions (id, user_id, content, asked_at) VALUES (23, 6, '학교 주차장 이용 요금은 얼마인가요?', '2026-11-09 13:45:03');
INSERT INTO answers (id, question_id, content, found) VALUES (23, 23, '문서에서 찾을 수 없음', FALSE);
INSERT INTO questions (id, user_id, content, asked_at) VALUES (24, 3, '졸업논문 제출 마감일은 언제인가요?', '2026-11-09 14:08:36');
INSERT INTO answers (id, question_id, content, found) VALUES (24, 24, '문서에서 찾을 수 없음', FALSE);

-- H2: 명시 id로 넣은 테이블의 자동 증가 값을 다음 번호로 맞춘다
ALTER TABLE users ALTER COLUMN id RESTART WITH 7;
ALTER TABLE categories ALTER COLUMN id RESTART WITH 5;
ALTER TABLE tags ALTER COLUMN id RESTART WITH 13;
ALTER TABLE documents ALTER COLUMN id RESTART WITH 8;
ALTER TABLE summaries ALTER COLUMN id RESTART WITH 7;
ALTER TABLE chunks ALTER COLUMN id RESTART WITH 30;
ALTER TABLE questions ALTER COLUMN id RESTART WITH 25;
ALTER TABLE answers ALTER COLUMN id RESTART WITH 25;
