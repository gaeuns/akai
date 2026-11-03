package com.tadak.akai.ai;

import com.tadak.akai.domain.Chunk;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Adapter: OpenAI API를 SummaryGenerator·AnswerGenerator로 감싼다. API 키는 여기서만 읽는다. */
@Component
public class OpenAiAdapter implements SummaryGenerator, AnswerGenerator {

    private final String apiKey;
    private final String model;

    public OpenAiAdapter(
            @Value("${openai.api-key:}") String apiKey, @Value("${openai.model:}") String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public String summarize(String text) {
        // TODO(F-02): 5문장 이내 요약
        throw new UnsupportedOperationException("F-02 구현 예정");
    }

    @Override
    public List<String> extractKeywords(String text) {
        // TODO(F-02): 핵심 키워드 5개
        throw new UnsupportedOperationException("F-02 구현 예정");
    }

    @Override
    public String generateAnswer(String question, List<Chunk> chunks) {
        // TODO(F-03): 전달받은 조각만 프롬프트에 넣어 답변 생성
        throw new UnsupportedOperationException("F-03 구현 예정");
    }
}
