package com.tadak.akai.domain;

import java.util.Locale;

public enum FileType {
    PDF,
    TXT;

    /** 파일 이름의 확장자로 형식을 정한다. PDF·TXT 외에는 거부한다. */
    public static FileType fromFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("파일 이름이 없습니다.");
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf")) {
            return PDF;
        }
        if (lower.endsWith(".txt")) {
            return TXT;
        }
        throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. PDF 또는 TXT만 올릴 수 있습니다.");
    }

    public String extension() {
        return name().toLowerCase(Locale.ROOT);
    }
}
