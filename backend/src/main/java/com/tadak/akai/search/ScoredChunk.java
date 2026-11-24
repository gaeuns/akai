package com.tadak.akai.search;

import com.tadak.akai.domain.Chunk;

/**
 * F-10: 검색 방식 비교 화면용 조각 점수. relevant는 그 검색 방식의 기준
 * (키워드 절반 이상·최소 2개, 유사도 기준값 이상)을 넘었는지다. 질문하기는 relevant인 조각만 근거로 쓴다.
 */
public record ScoredChunk(Chunk chunk, double score, boolean relevant) {
}
