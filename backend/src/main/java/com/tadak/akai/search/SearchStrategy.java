package com.tadak.akai.search;

import com.tadak.akai.domain.Chunk;
import java.util.List;

/** Strategy: 질문과 관련된 조각을 찾는다. MVP는 키워드, 확장 시 임베딩. */
public interface SearchStrategy {

    List<Chunk> search(String question);
}
