package com.tadak.akai.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

class ApiExceptionHandlerTest {

    @Test
    void 업로드_크기_초과는_413과_안내_문구() {
        ResponseEntity<Map<String, String>> response =
                new ApiExceptionHandler().tooLarge(new MaxUploadSizeExceededException(20L * 1024 * 1024));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(response.getBody()).containsEntry("message", "파일이 너무 큽니다. 20MB 이하만 올릴 수 있습니다.");
    }
}
