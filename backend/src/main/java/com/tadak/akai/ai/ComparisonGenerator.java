package com.tadak.akai.ai;

import java.util.List;

/** F-07: 여러 문서의 공통점과 차이점을 요약한다 (ISP: 요약·답변·임베딩과 분리) */
public interface ComparisonGenerator {

    /**
     * 전달받은 문서 내용만 근거로 비교한다.
     *
     * @throws AiServiceException AI 서비스가 응답하지 않을 때
     */
    String compare(List<ComparisonSource> sources);
}
