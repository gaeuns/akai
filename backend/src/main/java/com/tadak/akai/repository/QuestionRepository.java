package com.tadak.akai.repository;

import com.tadak.akai.domain.Question;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    /** F-05: 내 질문을 최신순으로. 같은 시각이면 나중에 저장한 것이 먼저 */
    List<Question> findByUserIdOrderByAskedAtDescIdDesc(Long userId);

    /** F-09: "문서에서 찾을 수 없음"으로 답한 질문 수 */
    @Query("select count(q) from Question q where q.answer.found = false")
    long countNotFound();

    /** F-09: 응답 시간이 기록된 질문의 평균(ms). 없으면 null */
    @Query("select avg(q.responseMillis) from Question q where q.responseMillis is not null")
    Double averageResponseMillis();

    @Query("select count(q) from Question q where q.responseMillis is not null")
    long countTimed();

    @Query("select count(q) from Question q where q.responseMillis is not null and q.responseMillis <= :limit")
    long countRespondedWithin(@Param("limit") long limitMillis);

    /** F-09: 근거로 많이 쓰인 문서 [문서 ID, 제목, 근거 횟수] */
    @Query("select c.document.id, c.document.title, count(c) from Answer a join a.evidences c "
            + "group by c.document.id, c.document.title order by count(c) desc")
    List<Object[]> countEvidenceByDocument(Pageable pageable);

    /** F-09: 최근에 문서에서 답을 못 찾은 질문 (어떤 문서가 더 필요한지 보는 용도) */
    List<Question> findTop5ByAnswerFoundFalseOrderByAskedAtDescIdDesc();
}
