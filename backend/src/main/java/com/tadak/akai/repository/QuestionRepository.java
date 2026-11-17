package com.tadak.akai.repository;

import com.tadak.akai.domain.Question;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    /** F-05: 내 질문을 최신순으로. 같은 시각이면 나중에 저장한 것이 먼저 */
    List<Question> findByUserIdOrderByAskedAtDescIdDesc(Long userId);
}
