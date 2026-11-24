package com.tadak.akai.search;

import com.tadak.akai.ai.AiServiceException;
import com.tadak.akai.ai.EmbeddingGenerator;
import com.tadak.akai.domain.Chunk;
import com.tadak.akai.repository.ChunkRepository;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * F-06 · Strategy: 질문과 조각을 임베딩으로 바꿔 코사인 유사도가 높은 조각을 찾는다.
 * 키워드가 달라도("학점"과 "등급") 뜻이 비슷하면 찾는다. 기준 유사도보다 낮은 조각만 있으면 빈 목록을 돌려주고,
 * QaService는 AI를 부르지 않고 "문서에서 찾을 수 없음"으로 답한다.
 *
 * <p>조각 임베딩은 업로드 때가 아니라 처음 검색할 때 계산해 저장한다(문서 처리 파이프라인은 그대로 둔다).
 * 임베딩 API가 응답하지 않으면 키워드 검색으로 대신해 질문하기가 멈추지 않게 한다.
 */
@Component
public class EmbeddingSearchStrategy implements SearchStrategy {

    static final int MAX_RESULTS = 3;

    private static final Logger log = LoggerFactory.getLogger(EmbeddingSearchStrategy.class);

    private final ChunkRepository chunkRepository;
    private final EmbeddingGenerator embeddingGenerator;
    private final KeywordSearchStrategy fallback;
    private final double minSimilarity;

    public EmbeddingSearchStrategy(
            ChunkRepository chunkRepository,
            EmbeddingGenerator embeddingGenerator,
            KeywordSearchStrategy fallback,
            @Value("${app.search.embedding.min-similarity:0.35}") double minSimilarity) {
        this.chunkRepository = chunkRepository;
        this.embeddingGenerator = embeddingGenerator;
        this.fallback = fallback;
        this.minSimilarity = minSimilarity;
    }

    @Override
    public List<Chunk> search(String question) {
        List<Chunk> chunks = chunkRepository.findAll();
        if (chunks.isEmpty()) {
            return List.of();
        }
        try {
            fillMissingEmbeddings(chunks);
            float[] questionVector = embed(List.of(question)).get(0);
            return chunks.stream()
                    .map(chunk -> new Scored(chunk, cosineSimilarity(questionVector, chunk.getEmbedding())))
                    .filter(scored -> scored.similarity() >= minSimilarity)
                    .sorted(Comparator.comparingDouble(Scored::similarity).reversed())
                    .limit(MAX_RESULTS)
                    .map(Scored::chunk)
                    .toList();
        } catch (AiServiceException e) {
            log.warn("임베딩 검색 실패, 키워드 검색으로 대신함: {}", e.getMessage());
            return fallback.search(question);
        }
    }

    /** 아직 임베딩이 없는 조각(새로 올린 문서)만 한 번에 계산해 저장한다. */
    private void fillMissingEmbeddings(List<Chunk> chunks) {
        List<Chunk> missing = chunks.stream().filter(chunk -> !chunk.hasEmbedding()).toList();
        if (missing.isEmpty()) {
            return;
        }
        List<float[]> vectors = embed(missing.stream().map(Chunk::getContent).toList());
        for (int i = 0; i < missing.size(); i++) {
            missing.get(i).assignEmbedding(vectors.get(i));
        }
        log.info("조각 {}개의 임베딩을 계산해 저장했습니다", missing.size());
    }

    private List<float[]> embed(List<String> texts) {
        List<float[]> vectors = embeddingGenerator.embedAll(texts);
        if (vectors == null || vectors.size() != texts.size()) {
            throw new AiServiceException("임베딩 응답이 비어 있거나 개수가 맞지 않습니다.");
        }
        return vectors;
    }

    static double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) {
            return 0;
        }
        double dot = 0;
        double normA = 0;
        double normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return (normA == 0 || normB == 0) ? 0 : dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private record Scored(Chunk chunk, double similarity) {
    }
}
