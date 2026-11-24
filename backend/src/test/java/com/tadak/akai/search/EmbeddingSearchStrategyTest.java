package com.tadak.akai.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tadak.akai.ai.AiServiceException;
import com.tadak.akai.ai.EmbeddingGenerator;
import com.tadak.akai.domain.Chunk;
import com.tadak.akai.domain.Document;
import com.tadak.akai.domain.FileType;
import com.tadak.akai.repository.ChunkRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

/** F-06 임베딩 검색: 유사도 기준, 임베딩 저장, 실패 시 키워드 검색 대체 */
class EmbeddingSearchStrategyTest {

    private static final double MIN_SIMILARITY = 0.35;

    private final ChunkRepository chunkRepository = mock(ChunkRepository.class);
    private final EmbeddingGenerator embeddingGenerator = mock(EmbeddingGenerator.class);
    private final KeywordSearchStrategy keywordSearch = mock(KeywordSearchStrategy.class);
    private final EmbeddingSearchStrategy strategy =
            new EmbeddingSearchStrategy(chunkRepository, embeddingGenerator, keywordSearch, MIN_SIMILARITY);
    private final Document document = new Document("규정", "rule.txt", FileType.TXT, null);

    @Test
    void 유사도가_기준_이상인_조각만_높은_순으로_돌려준다() {
        Chunk library = embedded("도서관", 1f, 0f);
        Chunk security = embedded("보안", 0f, 1f);
        Chunk mixed = embedded("도서관 보안", 1f, 1f);
        when(chunkRepository.findAll()).thenReturn(List.of(security, mixed, library));
        when(embeddingGenerator.embedAll(List.of("도서관 대출"))).thenReturn(List.<float[]>of(new float[] {1f, 0f}));

        List<Chunk> found = strategy.search("도서관 대출");

        assertThat(found).containsExactly(library, mixed);
    }

    @Test
    void 모든_조각이_기준보다_낮으면_빈_목록이다() {
        when(chunkRepository.findAll()).thenReturn(List.of(embedded("도서관", 1f, 0f)));
        when(embeddingGenerator.embedAll(List.of("기숙사"))).thenReturn(List.<float[]>of(new float[] {0f, 1f}));

        assertThat(strategy.search("기숙사")).isEmpty();
    }

    @Test
    void 임베딩이_없는_조각만_계산해_저장한다() {
        Chunk saved = embedded("이미 계산한 조각", 1f, 0f);
        Chunk fresh = new Chunk(document, 1, 2, "새로 올린 조각");
        when(chunkRepository.findAll()).thenReturn(List.of(saved, fresh));
        when(embeddingGenerator.embedAll(List.of("새로 올린 조각"))).thenReturn(List.<float[]>of(new float[] {0f, 1f}));
        when(embeddingGenerator.embedAll(List.of("질문"))).thenReturn(List.<float[]>of(new float[] {0f, 1f}));

        List<Chunk> found = strategy.search("질문");

        assertThat(fresh.getEmbedding()).containsExactly(0f, 1f);
        assertThat(found).containsExactly(fresh);
        verify(embeddingGenerator, never()).embedAll(List.of("이미 계산한 조각", "새로 올린 조각"));
    }

    @Test
    void 임베딩_API가_응답하지_않으면_키워드_검색으로_대신한다() {
        Chunk library = embedded("도서관", 1f, 0f);
        when(chunkRepository.findAll()).thenReturn(List.of(library));
        when(embeddingGenerator.embedAll(anyList())).thenThrow(new AiServiceException("시간 초과"));
        when(keywordSearch.search("도서관 대출")).thenReturn(List.of(library));

        assertThat(strategy.search("도서관 대출")).containsExactly(library);
    }

    @Test
    void 임베딩_응답_개수가_맞지_않으면_키워드_검색으로_대신한다() {
        when(chunkRepository.findAll()).thenReturn(List.of(embedded("도서관", 1f, 0f)));
        when(embeddingGenerator.embedAll(anyList())).thenReturn(List.of());
        when(keywordSearch.search("도서관")).thenReturn(List.of());

        assertThat(strategy.search("도서관")).isEmpty();
        verify(keywordSearch).search("도서관");
    }

    @Test
    void 조각이_없으면_임베딩_API를_부르지_않고_빈_목록이다() {
        when(chunkRepository.findAll()).thenReturn(List.of());

        assertThat(strategy.search("도서관")).isEmpty();
        verify(embeddingGenerator, never()).embedAll(anyList());
    }

    @Test
    void 코사인_유사도는_같은_방향이면_1_직각이면_0_길이가_다르면_0이다() {
        assertThat(EmbeddingSearchStrategy.cosineSimilarity(new float[] {1f, 1f}, new float[] {2f, 2f}))
                .isCloseTo(1.0, within(1e-6));
        assertThat(EmbeddingSearchStrategy.cosineSimilarity(new float[] {1f, 0f}, new float[] {0f, 1f})).isZero();
        assertThat(EmbeddingSearchStrategy.cosineSimilarity(new float[] {1f}, new float[] {1f, 0f})).isZero();
        assertThat(EmbeddingSearchStrategy.cosineSimilarity(null, new float[] {1f})).isZero();
    }

    private Chunk embedded(String content, float... vector) {
        Chunk chunk = new Chunk(document, 1, content.length(), content);
        chunk.assignEmbedding(vector);
        return chunk;
    }
}
