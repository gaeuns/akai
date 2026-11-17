package com.tadak.akai.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tadak.akai.ai.OpenAiAdapter;
import com.tadak.akai.config.LoginUser;
import com.tadak.akai.dto.SummaryUpdateRequest;
import com.tadak.akai.repository.DocumentRepository;
import com.tadak.akai.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** F-02 관리자 요약·키워드 수정 (TT-03). OpenAI는 Mockito로 대신한다. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:summary-edit-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.upload-dir=build/test-uploads-summary-edit"
})
class SummaryEditApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private OpenAiAdapter openAiAdapter;

    private Long documentId;

    @BeforeEach
    void setUp() throws Exception {
        documentRepository.deleteAll();
        when(openAiAdapter.summarize(anyString())).thenReturn("AI가 만든 요약");
        when(openAiAdapter.extractKeywords(anyString())).thenReturn(List.of("학사", "수강"));
        documentId = upload();
    }

    @Test
    void 관리자가_수정하면_저장되고_AI_생성_표시가_꺼지며_AI를_다시_부르지_않는다() throws Exception {
        edit("admin01", "관리자가 고친 요약", "학사,  휴학 ,,학사")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("관리자가 고친 요약"))
                .andExpect(jsonPath("$.keywords").value("학사, 휴학"))
                .andExpect(jsonPath("$.aiGenerated").value(false));

        mockMvc.perform(get("/api/documents/{id}", documentId).with(user(login("member01"))))
                .andExpect(jsonPath("$.summary").value("관리자가 고친 요약"))
                .andExpect(jsonPath("$.aiGenerated").value(false));
        verify(openAiAdapter, times(1)).summarize(anyString());
    }

    @Test
    void 이용자가_요약을_수정하면_403() throws Exception {
        edit("member01", "이용자가 고친 요약", "학사")
                .andExpect(status().isForbidden());
    }

    @Test
    void 빈_요약은_400() throws Exception {
        edit("admin01", "   ", "학사")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("요약을 입력하세요."));
    }

    @Test
    void 요약이_정확히_3000자면_저장된다() throws Exception {
        edit("admin01", "가".repeat(3000), "학사")
                .andExpect(status().isOk());
    }

    @Test
    void 요약이_3000자를_넘으면_400() throws Exception {
        edit("admin01", "가".repeat(3001), "학사")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("요약은 3000자 이내로 입력하세요."));
    }

    @Test
    void 키워드가_500자를_넘으면_400() throws Exception {
        edit("admin01", "요약", "가".repeat(501))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("키워드는 500자 이내로 입력하세요."));
    }

    @Test
    void 요약이_없던_문서에도_관리자가_써_넣을_수_있다() throws Exception {
        when(openAiAdapter.summarize(anyString())).thenThrow(new IllegalStateException("키 없음"));
        Long noSummaryId = upload();

        mockMvc.perform(put("/api/documents/{id}/summary", noSummaryId)
                        .with(user(login("admin01")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SummaryUpdateRequest("직접 쓴 요약", "학사"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("직접 쓴 요약"))
                .andExpect(jsonPath("$.aiGenerated").value(false));
    }

    @Test
    void 없는_문서의_요약을_수정하면_404() throws Exception {
        mockMvc.perform(put("/api/documents/{id}/summary", 9999)
                        .with(user(login("admin01")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SummaryUpdateRequest("요약", "학사"))))
                .andExpect(status().isNotFound());
    }

    private ResultActions edit(String loginId, String summary, String keywords) throws Exception {
        return mockMvc.perform(put("/api/documents/{id}/summary", documentId)
                .with(user(login(loginId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SummaryUpdateRequest(summary, keywords))));
    }

    private Long upload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "rule.txt", "text/plain", "학사 운영 규정 본문".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/documents").file(file).with(user(login("admin01"))))
                .andExpect(status().isCreated());
        return documentRepository.findAllByOrderByCreatedAtDesc().get(0).getId();
    }

    private LoginUser login(String loginId) {
        return LoginUser.from(userRepository.findByLoginId(loginId).orElseThrow());
    }
}
