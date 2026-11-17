package com.tadak.akai.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tadak.akai.ai.OpenAiAdapter;
import com.tadak.akai.config.LoginUser;
import com.tadak.akai.dto.QuestionRequest;
import com.tadak.akai.repository.DocumentRepository;
import com.tadak.akai.repository.QuestionRepository;
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

/** F-05 질의 이력 (HT-01, HT-02). OpenAI는 Mockito로 대신한다. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:history-api-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.upload-dir=build/test-uploads-history-api"
})
class HistoryApiTest {

    private static final String LIBRARY_RULE = "중앙도서관 대출 규정. 학부생은 도서를 5권까지 14일 동안 대출할 수 있다.";

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

    private Long documentId;

    @BeforeEach
    void setUp() throws Exception {
        questionRepository.deleteAll();
        documentRepository.deleteAll();
        when(openAiAdapter.summarize(anyString())).thenReturn("요약");
        when(openAiAdapter.extractKeywords(anyString())).thenReturn(List.of("키워드"));
        when(openAiAdapter.generateAnswer(anyString(), anyList())).thenReturn("학부생은 5권을 14일 동안 빌릴 수 있습니다.");
        MockMultipartFile file = new MockMultipartFile(
                "file", "library.txt", "text/plain", LIBRARY_RULE.getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/documents").file(file).with(user(login("admin01"))))
                .andExpect(status().isCreated());
        documentId = documentRepository.findAll().get(0).getId();
    }

    @Test
    void 질문하면_이력에_질문_답변_근거_일시가_최신순으로_남는다() throws Exception {
        ask("member01", "학부생 도서 대출 기간은?");
        ask("member01", "기숙사 입사 신청은 언제 하나요?");

        mockMvc.perform(get("/api/questions").with(user(login("member01"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].question").value("기숙사 입사 신청은 언제 하나요?"))
                .andExpect(jsonPath("$[0].answer").value("문서에서 찾을 수 없음"))
                .andExpect(jsonPath("$[0].found").value(false))
                .andExpect(jsonPath("$[1].question").value("학부생 도서 대출 기간은?"))
                .andExpect(jsonPath("$[1].answer").value("학부생은 5권을 14일 동안 빌릴 수 있습니다."))
                .andExpect(jsonPath("$[1].found").value(true))
                .andExpect(jsonPath("$[1].evidences[0].documentTitle").value("library"))
                .andExpect(jsonPath("$[1].evidences[0].pageNo").value(1))
                .andExpect(jsonPath("$[1].askedAt", notNullValue()));
    }

    @Test
    void 질문한_적_없는_계정은_빈_목록이고_남의_질문은_보이지_않는다() throws Exception {
        ask("member01", "학부생 도서 대출 기간은?");

        mockMvc.perform(get("/api/questions").with(user(login("member02"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void 근거_문서를_삭제해도_이력은_남는다() throws Exception {
        ask("member01", "학부생 도서 대출 기간은?");

        mockMvc.perform(delete("/api/documents/{id}", documentId).with(user(login("admin01"))))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/questions").with(user(login("member01"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].found").value(true))
                .andExpect(jsonPath("$[0].evidences", hasSize(0)));
    }

    @Test
    void 로그인_없이_이력을_조회하면_401() throws Exception {
        mockMvc.perform(get("/api/questions"))
                .andExpect(status().isUnauthorized());
    }

    private void ask(String loginId, String question) throws Exception {
        mockMvc.perform(post("/api/questions")
                        .with(user(login(loginId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new QuestionRequest(question))))
                .andExpect(status().isCreated());
    }

    private LoginUser login(String loginId) {
        return LoginUser.from(userRepository.findByLoginId(loginId).orElseThrow());
    }
}
