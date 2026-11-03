package com.tadak.akai.ai;

import java.util.List;

/** 문서 요약과 키워드 생성 (ISP: 답변 생성과 분리) */
public interface SummaryGenerator {

    String summarize(String text);

    List<String> extractKeywords(String text);
}
