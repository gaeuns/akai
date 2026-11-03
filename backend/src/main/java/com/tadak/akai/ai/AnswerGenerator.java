package com.tadak.akai.ai;

import com.tadak.akai.domain.Chunk;
import java.util.List;

/** 검색된 조각만 근거로 답변을 생성한다 (ISP: 요약과 분리) */
public interface AnswerGenerator {

    String generateAnswer(String question, List<Chunk> chunks);
}
