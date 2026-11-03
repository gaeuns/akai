package com.tadak.akai.service;

import com.tadak.akai.ai.AnswerGenerator;
import com.tadak.akai.domain.Answer;
import com.tadak.akai.domain.Question;
import com.tadak.akai.domain.User;
import com.tadak.akai.repository.QuestionRepository;
import com.tadak.akai.search.SearchStrategy;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class QaService {

    /** 관련 조각이 없을 때 AI를 호출하지 않고 반환하는 고정 문구 */
    public static final String NOT_FOUND_MESSAGE = "문서에서 찾을 수 없음";

    private final SearchStrategy searchStrategy;
    private final AnswerGenerator answerGenerator;
    private final QuestionRepository questionRepository;

    public QaService(
            SearchStrategy searchStrategy,
            AnswerGenerator answerGenerator,
            QuestionRepository questionRepository) {
        this.searchStrategy = searchStrategy;
        this.answerGenerator = answerGenerator;
        this.questionRepository = questionRepository;
    }

    public Answer ask(String question, User user) {
        // TODO(F-03): 검색 → 조각 없으면 NOT_FOUND_MESSAGE(found=false) → 있으면 AI 답변 → 근거와 함께 저장
        throw new UnsupportedOperationException("F-03 구현 예정");
    }

    public List<Question> history(User user) {
        // TODO(F-05): 질의 이력 최신순 조회
        throw new UnsupportedOperationException("F-05 구현 예정");
    }
}
