package com.tadak.akai.search;

import com.tadak.akai.domain.Chunk;
import com.tadak.akai.repository.ChunkRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class KeywordSearchStrategy implements SearchStrategy {

    private final ChunkRepository chunkRepository;

    public KeywordSearchStrategy(ChunkRepository chunkRepository) {
        this.chunkRepository = chunkRepository;
    }

    @Override
    public List<Chunk> search(String question) {
        // TODO(F-03): 질문에서 키워드를 뽑아 조각 검색, 관련도 순으로 정렬
        throw new UnsupportedOperationException("F-03 구현 예정");
    }
}
