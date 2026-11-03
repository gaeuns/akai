package com.tadak.akai.repository;

import com.tadak.akai.domain.Question;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByUserIdOrderByAskedAtDesc(Long userId);
}
