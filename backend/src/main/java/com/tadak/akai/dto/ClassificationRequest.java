package com.tadak.akai.dto;

import java.util.List;

/** 문서에 분류·태그 붙이기 요청 (F-04). categoryId가 null이면 미분류, tagIds가 비면 태그를 모두 뗀다. */
public record ClassificationRequest(Long categoryId, List<Long> tagIds) {
}
