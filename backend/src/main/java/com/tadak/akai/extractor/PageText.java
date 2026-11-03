package com.tadak.akai.extractor;

/** 추출한 텍스트 한 페이지. TXT는 전체를 1페이지로 본다. */
public record PageText(int pageNo, String text) {
}
