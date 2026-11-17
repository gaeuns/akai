package com.tadak.akai.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tadak.akai.ai.OpenAiAdapter;
import com.tadak.akai.config.LoginUser;
import com.tadak.akai.domain.Document;
import com.tadak.akai.repository.DocumentRepository;
import com.tadak.akai.repository.UserRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** 문서 목록·검색(F-04), 상세(F-02), 삭제(F-01) API. OpenAI는 Mockito로 대신한다. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:document-api-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.upload-dir=build/test-uploads-document-api"
})
class DocumentApiTest {

    private static final Path UPLOAD_DIR = Paths.get("build/test-uploads-document-api");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private OpenAiAdapter openAiAdapter;

    @BeforeEach
    void clean() throws IOException {
        documentRepository.deleteAll();
        for (Path file : uploadedFiles()) {
            Files.delete(file);
        }
        when(openAiAdapter.summarize(anyString())).thenReturn("AI가 만든 요약");
        when(openAiAdapter.extractKeywords(anyString())).thenReturn(List.of("기본"));
        when(openAiAdapter.extractKeywords(contains("도서관"))).thenReturn(List.of("도서관", "대출", "열람실", "연체", "분실"));
        when(openAiAdapter.extractKeywords(contains("보안"))).thenReturn(List.of("보안", "비밀번호", "백신", "개인정보", "USB"));
    }

    @Test
    void 키워드로_검색하면_일치하는_문서만_나온다() throws Exception {
        upload("library.txt", "도서관 이용 규정 본문", null);
        upload("security.txt", "정보 보안 매뉴얼 본문", null);

        mockMvc.perform(get("/api/documents").param("keyword", "열람실").with(user(member())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("library"))
                .andExpect(jsonPath("$[0].aiGenerated").value(true));
    }

    @Test
    void 제목_검색은_대소문자를_가리지_않는다() throws Exception {
        upload("security.txt", "정보 보안 매뉴얼 본문", "USB Policy");

        mockMvc.perform(get("/api/documents").param("keyword", "usb policy").with(user(member())))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("USB Policy"));
    }

    @Test
    void 검색어가_없으면_전체를_최신순으로_보여_준다() throws Exception {
        upload("library.txt", "도서관 이용 규정 본문", null);
        upload("security.txt", "정보 보안 매뉴얼 본문", null);

        mockMvc.perform(get("/api/documents").with(user(member())))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("security"))
                .andExpect(jsonPath("$[1].title").value("library"));
    }

    @Test
    void 일치하는_문서가_없으면_빈_목록이다() throws Exception {
        upload("library.txt", "도서관 이용 규정 본문", null);

        mockMvc.perform(get("/api/documents").param("keyword", "기숙사").with(user(member())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void 상세는_요약_키워드_조각을_AI_생성_표시와_함께_돌려준다() throws Exception {
        Long id = upload("library.txt", "도서관 이용 규정 본문", "도서관 규정");

        mockMvc.perform(get("/api/documents/{id}", id).with(user(member())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("도서관 규정"))
                .andExpect(jsonPath("$.summary").value("AI가 만든 요약"))
                .andExpect(jsonPath("$.keywords").value("도서관, 대출, 열람실, 연체, 분실"))
                .andExpect(jsonPath("$.aiGenerated").value(true))
                .andExpect(jsonPath("$.chunks[0].pageNo").value(1));
    }

    @Test
    void 없는_문서의_상세는_404() throws Exception {
        mockMvc.perform(get("/api/documents/{id}", 9999).with(user(member())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("문서를 찾을 수 없습니다: 9999"));
    }

    @Test
    void 문서를_삭제하면_목록과_업로드_폴더에서_사라진다() throws Exception {
        Long id = upload("library.txt", "도서관 이용 규정 본문", null);

        mockMvc.perform(delete("/api/documents/{id}", id).with(user(admin())))
                .andExpect(status().isNoContent());

        assertThat(documentRepository.count()).isZero();
        assertThat(uploadedFiles()).isEmpty();
    }

    @Test
    void 없는_문서를_삭제하면_404() throws Exception {
        mockMvc.perform(delete("/api/documents/{id}", 9999).with(user(admin())))
                .andExpect(status().isNotFound());
    }

    private Long upload(String fileName, String content, String title) throws Exception {
        MockMultipartFile file =
                new MockMultipartFile("file", fileName, "text/plain", content.getBytes(StandardCharsets.UTF_8));
        var request = multipart("/api/documents").file(file).with(user(admin()));
        if (title != null) {
            request.param("title", title);
        }
        mockMvc.perform(request).andExpect(status().isCreated());
        return documentRepository.findAllByOrderByCreatedAtDesc().stream().map(Document::getId).findFirst().orElseThrow();
    }

    private LoginUser member() {
        return LoginUser.from(userRepository.findByLoginId("member01").orElseThrow());
    }

    private LoginUser admin() {
        return LoginUser.from(userRepository.findByLoginId("admin01").orElseThrow());
    }

    private List<Path> uploadedFiles() throws IOException {
        if (!Files.exists(UPLOAD_DIR)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(UPLOAD_DIR)) {
            return files.toList();
        }
    }
}
