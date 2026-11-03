# db

ERD(시스템 설계서 8절) 기반 스키마와 샘플 데이터를 둔다.

- `schema.sql`: 테이블 생성 SQL (WBS 3.5, 임성용, 12주차)
- `sample-data.sql`: 시연용 계정·분류·태그 샘플 데이터

개발 중에는 JPA `ddl-auto=update`로 테이블을 만들고, 스키마 SQL과 엔티티가 일치하는지 리뷰한다.
