package com.tadak.akai.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tadak.akai.ai.AiServiceException;
import com.tadak.akai.ai.ComparisonSource;
import com.tadak.akai.ai.OpenAiAdapter;
import com.tadak.akai.config.LoginUser;
import com.tadak.akai.dto.ComparisonRequest;
import com.tadak.akai.repository.DocumentRepository;
import com.tadak.akai.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** F-07 여러 문서 비교 요약. OpenAI는 Mockito로 대신한다. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:comparison-api-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.upload-dir=build/test-uploads-comparison-api"
})
class ComparisonApiTest {

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

    private Long libraryId;
    private Long securityId;

    @BeforeEach
    void setUp() throws Exception {
        documentRepository.deleteAll();
        when(openAiAdapter.summarize(anyString())).thenReturn("저장된 요약");
        when(openAiAdapter.extractKeywords(anyString())).thenReturn(List.of("규정"));
        when(openAiAdapter.compare(anyList())).thenReturn("공통점\n- 둘 다 규정이다");
        libraryId = upload("library.txt", "도서관 대출 규정 본문");
        securityId = upload("security.txt", "정보보안 매뉴얼 본문");
    }

    @Test
    void 이용자가_문서_2개를_비교하면_저장된_요약으로_AI_생성_결과를_돌려준다() throws Exception {
        compare("member01", libraryId, securityId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comparison").value("공통점\n- 둘 다 규정이다"))
                .andExpect(jsonPath("$.aiGenerated").value(true))
                .andExpect(jsonPath("$.documents", hasSize(2)))
                .andExpect(jsonPath("$.documents[0].usedSummary").value(true));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ComparisonSource>> sent = ArgumentCaptor.forClass(List.class);
        verify(openAiAdapter).compare(sent.capture());
        assertThat(sent.getValue()).extracting(ComparisonSource::content).allMatch(c -> c.startsWith("저장된 요약"));
    }

    @Test
    void 요약이_없는_문서는_본문으로_비교하고_표시한다() throws Exception {
        when(openAiAdapter.summarize(anyString())).thenThrow(new IllegalStateException("키 없음"));
        Long noSummaryId = upload("plain.txt", "요약 없는 문서의 본문");

        compare("member01", libraryId, noSummaryId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documents[1].usedSummary").value(false));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ComparisonSource>> sent = ArgumentCaptor.forClass(List.class);
        verify(openAiAdapter).compare(sent.capture());
        assertThat(sent.getValue().get(1).content()).isEqualTo("요약 없는 문서의 본문");
    }

    @Test
    void 문서를_1개만_고르면_400() throws Exception {
        compare("member01", libraryId)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비교할 문서를 2개 이상 고르세요."));
        verify(openAiAdapter, never()).compare(anyList());
    }

    @Test
    void 같은_문서를_두_번_고르면_1개로_보고_400() throws Exception {
        compare("member01", libraryId, libraryId)
                .andExpect(status().isBadRequest());
    }

    @Test
    void 문서를_6개_고르면_400() throws Exception {
        compare("member01", 1L, 2L, 3L, 4L, 5L, 6L)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("한 번에 5개까지 비교할 수 있습니다."));
    }

    @Test
    void 없는_문서가_섞이면_404() throws Exception {
        compare("member01", libraryId, 9999L)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("문서를 찾을 수 없습니다: 9999"));
    }

    @Test
    void AI_서비스가_응답하지_않으면_503과_재시도_안내() throws Exception {
        when(openAiAdapter.compare(anyList())).thenThrow(new AiServiceException("시간 초과"));

        compare("member01", libraryId, securityId)
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value(containsString("다시 시도")));
    }

    @Test
    void 로그인하지_않으면_401() throws Exception {
        mockMvc.perform(post("/api/comparisons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ComparisonRequest(List.of(libraryId, securityId)))))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions compare(String loginId, Long... ids) throws Exception {
        return mockMvc.perform(post("/api/comparisons")
                .with(user(login(loginId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ComparisonRequest(Arrays.asList(ids)))));
    }

    private Long upload(String fileName, String content) throws Exception {
        MockMultipartFile file =
                new MockMultipartFile("file", fileName, "text/plain", content.getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/documents").file(file).with(user(login("admin01"))))
                .andExpect(status().isCreated());
        return documentRepository.findAllByOrderByCreatedAtDesc().get(0).getId();
    }

    private LoginUser login(String loginId) {
        return LoginUser.from(userRepository.findByLoginId(loginId).orElseThrow());
    }
}
