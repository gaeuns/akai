package com.tadak.akai.dto;

/** F-02: 관리자 요약 수정. keywords는 쉼표로 구분한다. */
public record SummaryUpdateRequest(String summary, String keywords) {
}
