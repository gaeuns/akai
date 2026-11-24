package com.tadak.akai.dto;

import java.util.List;

/** F-07: 비교할 문서 ID 2~5개 */
public record ComparisonRequest(List<Long> documentIds) {
}
