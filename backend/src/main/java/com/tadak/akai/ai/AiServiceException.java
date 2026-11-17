package com.tadak.akai.ai;

/** AI 서비스가 응답하지 않거나 호출할 수 없을 때. 화면에는 오류 안내와 재시도 제안을 보여 준다. */
public class AiServiceException extends RuntimeException {

    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
