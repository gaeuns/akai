package com.tadak.akai.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
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
import com.tadak.akai.ai.OpenAiAdapter;
import com.tadak.akai.config.LoginUser;
import com.tadak.akai.domain.Chunk;
import com.tadak.akai.dto.QuestionRequest;
import com.tadak.akai.repository.DocumentRepository;
import com.tadak.akai.repository.QuestionRepository;
import com.tadak.akai.repository.UserRepository;
import java.nio.charset.StandardCharsets;
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
import org.springframework.transaction.annotation.Transactional;

/** F-03 근거 기반 Q&A (QT-01~QT-06). OpenAI는 Mockito로 대신하고, 문서는 업로드 API로 넣는다. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:qa-api-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.upload-dir=build/test-uploads-qa-api"
})
class QaApiTest {

    private static final String LIBRARY_RULE = "중앙도서관 대출 규정. 학부생은 도서를 5권까지 14일 동안 대출할 수 있다.";
    private static final String SECURITY_RULE = "정보보안 매뉴얼. 비밀번호는 90일마다 변경한다.";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private OpenAiAdapter openAiAdapter;

    @BeforeEach
    void setUp() throws Exception {
        questionRepository.deleteAll();
        documentRepository.deleteAll();
        when(openAiAdapter.summarize(anyString())).thenReturn("요약");
        when(openAiAdapter.extractKeywords(anyString())).thenReturn(List.of("키워드"));
        upload("library.txt", LIBRARY_RULE);
        upload("security.txt", SECURITY_RULE);
    }

    @Test
    void 문서에_있는_내용을_물으면_답변과_근거를_AI_생성_표시와_함께_돌려준다() throws Exception {
        when(openAiAdapter.generateAnswer(anyString(), anyList())).thenReturn("학부생은 5권을 14일 동안 빌릴 수 있습니다.");

        ask("학부생 도서 대출 기간은?")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.answer").value("학부생은 5권을 14일 동안 빌릴 수 있습니다."))
                .andExpect(jsonPath("$.found").value(true))
                .andExpect(jsonPath("$.aiGenerated").value(true))
                .andExpect(jsonPath("$.evidences", hasSize(1)))
                .andExpect(jsonPath("$.evidences[0].documentTitle").value("library"))
                .andExpect(jsonPath("$.evidences[0].pageNo").value(1))
                .andExpect(jsonPath("$.evidences[0].content").value(LIBRARY_RULE));

        assertThat(questionRepository.count()).isEqualTo(1);
    }

    @Test
    @Transactional // 저장된 답변의 found 값을 읽으려고 같은 트랜잭션에서 실행
    void 관련_조각이_없으면_AI를_부르지_않고_찾을_수_없음으로_답한다() throws Exception {
        ask("기숙사 입사 신청은 언제 하나요?")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.answer").value("문서에서 찾을 수 없음"))
                .andExpect(jsonPath("$.found").value(false))
                .andExpect(jsonPath("$.aiGenerated").value(false))
                .andExpect(jsonPath("$.evidences", hasSize(0)));

        verify(openAiAdapter, never()).generateAnswer(anyString(), anyList());
        assertThat(questionRepository.findAll().get(0).getAnswer().isFound()).isFalse();
    }

    @Test
    void AI가_근거에_답이_없다고_하면_found는_false다() throws Exception {
        when(openAiAdapter.generateAnswer(anyString(), anyList())).thenReturn("\"문서에서 찾을 수 없음\"");

        ask("학부생 도서 연체료는?")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.answer").value("문서에서 찾을 수 없음"))
                .andExpect(jsonPath("$.found").value(false))
                .andExpect(jsonPath("$.evidences", hasSize(0)));
    }

    @Test
    void AI_서비스가_응답하지_않으면_503과_재시도_안내() throws Exception {
        when(openAiAdapter.generateAnswer(anyString(), anyList())).thenThrow(new AiServiceException("시간 초과"));

        ask("학부생 도서 대출 기간은?")
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value(containsString("다시 시도")));

        assertThat(questionRepository.count()).isZero();
    }

    @Test
    void AI에는_검색된_조각만_보낸다() throws Exception {
        when(openAiAdapter.generateAnswer(anyString(), anyList())).thenReturn("답변");

        ask("학부생 도서 대출 기간은?").andExpect(status().isCreated());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Chunk>> sent = ArgumentCaptor.forClass(List.class);
        verify(openAiAdapter).generateAnswer(any(), sent.capture());
        assertThat(sent.getValue()).extracting(Chunk::getContent).containsExactly(LIBRARY_RULE);
    }

    @Test
    void 빈_질문은_400() throws Exception {
        ask("   ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("질문을 입력하세요."));
    }

    @Test
    void 질문이_1000자를_넘으면_400() throws Exception {
        ask("가".repeat(1001))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("질문은 1000자 이내로 입력하세요."));
    }

    @Test
    void 질문이_정확히_1000자면_받아들인다() throws Exception {
        ask("가".repeat(1000)).andExpect(status().isCreated());
    }

    private ResultActions ask(String question) throws Exception {
        return mockMvc.perform(post("/api/questions")
                .with(user(login("member01")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new QuestionRequest(question))));
    }

    private void upload(String fileName, String content) throws Exception {
        MockMultipartFile file =
                new MockMultipartFile("file", fileName, "text/plain", content.getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/documents").file(file).with(user(login("admin01"))))
                .andExpect(status().isCreated());
    }

    private LoginUser login(String loginId) {
        return LoginUser.from(userRepository.findByLoginId(loginId).orElseThrow());
    }
}
