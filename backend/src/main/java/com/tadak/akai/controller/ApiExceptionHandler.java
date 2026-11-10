package com.tadak.akai.controller;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/** 예외를 {"message": "..."} 형태의 JSON으로 바꿔 화면이 안내 문구를 보여 줄 수 있게 한다. */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
        return body(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> notFound(NoSuchElementException e) {
        return body(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> tooLarge(MaxUploadSizeExceededException e) {
        return body(HttpStatus.PAYLOAD_TOO_LARGE, "파일이 너무 큽니다. 20MB 이하만 올릴 수 있습니다.");
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, String>> fileError(IOException e) {
        log.error("파일 처리 오류", e);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "파일을 읽거나 저장하지 못했습니다. 손상된 파일인지 확인하세요.");
    }

    private ResponseEntity<Map<String, String>> body(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(Map.of("message", Objects.requireNonNullElse(message, status.getReasonPhrase())));
    }
}
