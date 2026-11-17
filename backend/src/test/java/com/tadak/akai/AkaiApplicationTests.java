package com.tadak.akai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/** 개발 DB(backend/data)를 쓰면 서버가 켜져 있을 때 파일 잠금으로 실패하므로 메모리 H2를 쓴다. */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:context-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AkaiApplicationTests {

    @Test
    void 설정이_올바르면_서버가_기동된다() {
    }
}
