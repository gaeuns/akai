package com.tadak.akai.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tadak.akai.repository.DocumentRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** 업로드 오류 케이스: 실패한 업로드는 응답이 4xx이고, DB와 업로드 폴더에 아무것도 남지 않아야 한다. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:upload-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.upload-dir=build/test-uploads",
    "openai.api-key="
})
class DocumentUploadApiTest {

    private static final Path UPLOAD_DIR = Paths.get("build/test-uploads");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DocumentRepository documentRepository;

    @BeforeEach
    void clean() throws IOException {
        documentRepository.deleteAll();
        for (Path file : uploadedFiles()) {
            Files.delete(file);
        }
    }

    @Test
    void 빈_파일은_400() throws Exception {
        upload(new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("빈 파일은 올릴 수 없습니다."));

        assertNothingSaved();
    }

    @Test
    void 지원하지_않는_형식은_400() throws Exception {
        upload(new MockMultipartFile("file", "report.docx", "application/octet-stream", new byte[] {1, 2, 3}))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("PDF 또는 TXT")));

        assertNothingSaved();
    }

    @Test
    void 텍스트_없는_PDF는_400이고_문서와_파일이_남지_않는다() throws Exception {
        upload(pdf("blank.pdf", blankPdf(false)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("스캔한 이미지 PDF")));

        assertNothingSaved();
    }

    @Test
    void 손상된_PDF는_4xx이고_문서와_파일이_남지_않는다() throws Exception {
        upload(pdf("broken.pdf", "PDF가 아닌 내용".getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().is4xxClientError());

        assertNothingSaved();
    }

    @Test
    void 암호_PDF는_400과_암호_안내() throws Exception {
        upload(pdf("locked.pdf", blankPdf(true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("암호")));

        assertNothingSaved();
    }

    @Test
    void 공백만_있는_TXT는_스캔_PDF_문구_없이_400() throws Exception {
        upload(new MockMultipartFile("file", "blank.txt", "text/plain", "   \n\n  ".getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("문서에서 텍스트를 찾을 수 없습니다."));

        assertNothingSaved();
    }

    @Test
    void 정상_TXT는_201이고_요약_없이도_READY로_저장된다() throws Exception {
        byte[] content = "제1조(목적) 이 규정은 학사 운영에 필요한 사항을 정한다.".getBytes(StandardCharsets.UTF_8);

        upload(new MockMultipartFile("file", "rule.txt", "text/plain", content))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.chunks.length()").value(1));

        assertThat(documentRepository.count()).isEqualTo(1);
        assertThat(uploadedFiles()).hasSize(1);
    }

    private ResultActions upload(MockMultipartFile file) throws Exception {
        return mockMvc.perform(multipart("/api/documents").file(file));
    }

    private MockMultipartFile pdf(String name, byte[] content) {
        return new MockMultipartFile("file", name, "application/pdf", content);
    }

    private byte[] blankPdf(boolean encrypted) throws IOException {
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            if (encrypted) {
                StandardProtectionPolicy policy =
                        new StandardProtectionPolicy("owner", "user", new AccessPermission());
                policy.setEncryptionKeyLength(128);
                document.protect(policy);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }

    private void assertNothingSaved() throws IOException {
        assertThat(documentRepository.count()).isZero();
        assertThat(uploadedFiles()).isEmpty();
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
