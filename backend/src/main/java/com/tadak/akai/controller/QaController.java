package com.tadak.akai.controller;

import com.tadak.akai.config.LoginUser;
import com.tadak.akai.dto.AnswerView;
import com.tadak.akai.dto.QuestionRequest;
import com.tadak.akai.service.QaService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions")
public class QaController {

    private final QaService qaService;

    public QaController(QaService qaService) {
        this.qaService = qaService;
    }

    /** UC-07 · F-03: 질문하기. 답변과 근거를 돌려주고 이력에 저장한다. */
    @PostMapping
    public ResponseEntity<AnswerView> ask(
            @RequestBody QuestionRequest request, @AuthenticationPrincipal LoginUser loginUser) {
        AnswerView answer = qaService.ask(request.question(), loginUser.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(answer);
    }

    /** UC-09 · F-05: 내 질의 이력 (최신순) */
    @GetMapping
    public List<AnswerView> history(@AuthenticationPrincipal LoginUser loginUser) {
        return qaService.history(loginUser.getUserId());
    }
}
