package com.tadak.akai.service;

import com.tadak.akai.ai.AnswerGenerator;
import com.tadak.akai.domain.Answer;
import com.tadak.akai.domain.Chunk;
import com.tadak.akai.domain.Question;
import com.tadak.akai.domain.User;
import com.tadak.akai.dto.AnswerView;
import com.tadak.akai.repository.QuestionRepository;
import com.tadak.akai.repository.UserRepository;
import com.tadak.akai.search.SearchStrategy;
import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** UC-07 · F-03: 검색 → (조각이 있을 때만) AI 답변 → 근거와 함께 이력에 저장 */
@Service
@Transactional(readOnly = true)
public class QaService {

    /** 관련 조각이 없을 때 AI를 호출하지 않고 반환하는 고정 문구 */
    public static final String NOT_FOUND_MESSAGE = AnswerGenerator.NOT_FOUND_MESSAGE;
    public static final int MAX_QUESTION_LENGTH = 1000;

    private static final Logger log = LoggerFactory.getLogger(QaService.class);

    private final SearchStrategy searchStrategy;
    private final AnswerGenerator answerGenerator;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    public QaService(
            SearchStrategy searchStrategy,
            AnswerGenerator answerGenerator,
            QuestionRepository questionRepository,
            UserRepository userRepository) {
        this.searchStrategy = searchStrategy;
        this.answerGenerator = answerGenerator;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    /**
     * 관련 조각이 없으면 AI를 부르지 않고 고정 문구로 답한다(found=false).
     * AI가 응답하지 않으면 AiServiceException이 그대로 올라가고 질문은 저장되지 않는다.
     */
    @Transactional
    public AnswerView ask(String questionText, Long userId) {
        String text = validate(questionText);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + userId));
        long start = System.nanoTime();

        List<Chunk> chunks = searchStrategy.search(text);
        long searched = System.nanoTime();
        Question question = new Question(user, text);
        question.assignAnswer(createAnswer(question, chunks));
        questionRepository.save(question);

        long answered = System.nanoTime();
        log.info("질문 응답 완료: 근거 {}개, found={} | 검색 {}, 답변 {} | 전체 {} (목표 5초 이내)",
                question.getAnswer().getEvidences().size(), question.getAnswer().isFound(),
                seconds(start, searched), seconds(searched, answered), seconds(start, answered));
        return AnswerView.from(question);
    }

    public List<Question> history(User user) {
        // TODO(F-05): 질의 이력 최신순 조회
        throw new UnsupportedOperationException("F-05 구현 예정");
    }

    private String validate(String questionText) {
        if (questionText == null || questionText.isBlank()) {
            throw new IllegalArgumentException("질문을 입력하세요.");
        }
        String text = questionText.strip();
        if (text.length() > MAX_QUESTION_LENGTH) {
            throw new IllegalArgumentException("질문은 " + MAX_QUESTION_LENGTH + "자 이내로 입력하세요.");
        }
        return text;
    }

    private Answer createAnswer(Question question, List<Chunk> chunks) {
        if (chunks.isEmpty()) {
            return notFound(question);
        }
        String generated = answerGenerator.generateAnswer(question.getContent(), chunks);
        if (isNotFoundReply(generated)) {
            return notFound(question);
        }
        return new Answer(question, generated, true, chunks);
    }

    /** 조각은 있었지만 AI가 근거로 답할 수 없다고 한 경우 (따옴표를 붙여 답해도 같은 것으로 본다) */
    private boolean isNotFoundReply(String generated) {
        return generated.replaceAll("^[\\s\"'“”]+", "").startsWith(NOT_FOUND_MESSAGE) || generated.isBlank();
    }

    private Answer notFound(Question question) {
        return new Answer(question, NOT_FOUND_MESSAGE, false, List.of());
    }

    private String seconds(long from, long to) {
        return String.format("%.1f초", (to - from) / 1_000_000_000.0);
    }
}
