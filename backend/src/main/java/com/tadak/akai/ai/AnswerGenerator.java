package com.tadak.akai.ai;

import com.tadak.akai.domain.Chunk;
import java.util.List;

/** 검색된 조각만 근거로 답변을 생성한다 (ISP: 요약과 분리) */
public interface AnswerGenerator {

    /** 근거에 답이 없을 때의 고정 문구. AI도 이 문구로만 답하게 지시한다. */
    String NOT_FOUND_MESSAGE = "문서에서 찾을 수 없음";

    /**
     * @throws AiServiceException AI 서비스가 응답하지 않을 때
     */
    String generateAnswer(String question, List<Chunk> chunks);
}
