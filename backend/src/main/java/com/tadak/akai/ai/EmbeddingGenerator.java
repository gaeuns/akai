package com.tadak.akai.ai;

import java.util.List;

/** F-06: 글을 의미 벡터(임베딩)로 바꾼다 (ISP: 요약·답변과 분리) */
public interface EmbeddingGenerator {

    /**
     * 입력 순서대로 벡터를 돌려준다.
     *
     * @throws AiServiceException AI 서비스가 응답하지 않을 때
     */
    List<float[]> embedAll(List<String> texts);
}
