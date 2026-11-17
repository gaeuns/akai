package com.tadak.akai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

// 개발 DB(backend/data)를 쓰지 않는다 (코딩 규칙 4절)
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:context-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AkaiApplicationTests {

    @Test
    void contextLoads() {
    }
}
