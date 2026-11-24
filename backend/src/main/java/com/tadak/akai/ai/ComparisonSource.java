package com.tadak.akai.ai;

/** F-07: 비교에 보낼 문서 하나. content는 저장된 요약(없으면 본문 앞부분)이다. */
public record ComparisonSource(String title, String content) {
}
