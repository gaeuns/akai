package com.tadak.akai.service;

import com.tadak.akai.ai.AiServiceException;
import com.tadak.akai.dto.SearchComparisonView;
import com.tadak.akai.dto.SearchComparisonView.ScoredEvidence;
import com.tadak.akai.dto.SearchComparisonView.StrategyResult;
import com.tadak.akai.search.EmbeddingSearchStrategy;
import com.tadak.akai.search.KeywordSearchStrategy;
import com.tadak.akai.search.ScoredChunk;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * F-10: 같은 질문을 두 검색 방식(Strategy)으로 찾아 점수와 함께 나란히 보여 준다(관리자).
 * 답변은 만들지 않고 검색만 한다. 질문하기에서 쓰는 방식은 app.search.strategy 설정 그대로다.
 */
@Service
public class SearchComparisonService {

    /** 방식마다 보여 줄 조각 수 (기준 미달 포함) */
    static final int RESULT_LIMIT = 5;

    private final KeywordSearchStrategy keywordSearchStrategy;
    private final EmbeddingSearchStrategy embeddingSearchStrategy;

    public SearchComparisonService(
            KeywordSearchStrategy keywordSearchStrategy, EmbeddingSearchStrategy embeddingSearchStrategy) {
        this.keywordSearchStrategy = keywordSearchStrategy;
        this.embeddingSearchStrategy = embeddingSearchStrategy;
    }

    /** 임베딩이 없는 조각은 이때 계산해 저장하므로 쓰기 트랜잭션으로 연다. */
    @Transactional
    public SearchComparisonView compare(String questionText) {
        if (questionText == null || questionText.isBlank()) {
            throw new IllegalArgumentException("질문을 입력하세요.");
        }
        String question = questionText.strip();
        return new SearchComparisonView(question, searchByKeyword(question), searchByEmbedding(question));
    }

    private StrategyResult searchByKeyword(String question) {
        long start = System.nanoTime();
        List<ScoredChunk> ranked = keywordSearchStrategy.rank(question, RESULT_LIMIT);
        return new StrategyResult(
                "키워드 절반 이상 포함",
                KeywordSearchStrategy.extractKeywords(question),
                elapsedMillis(start),
                null,
                ranked.stream().map(ScoredEvidence::from).toList());
    }

    private StrategyResult searchByEmbedding(String question) {
        String criterion = String.format("유사도 %.2f 이상", embeddingSearchStrategy.getMinSimilarity());
        long start = System.nanoTime();
        try {
            List<ScoredChunk> ranked = embeddingSearchStrategy.rank(question, RESULT_LIMIT);
            return new StrategyResult(
                    criterion, null, elapsedMillis(start), null, ranked.stream().map(ScoredEvidence::from).toList());
        } catch (AiServiceException e) {
            return new StrategyResult(
                    criterion, null, elapsedMillis(start), "임베딩 검색 실패: " + e.getMessage(), List.of());
        }
    }

    private long elapsedMillis(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }
}
