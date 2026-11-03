package com.tadak.akai.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions")
public class QaController {

    @PostMapping
    public ResponseEntity<Void> ask() {
        // TODO(F-03): 질문하기
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping
    public ResponseEntity<Void> history() {
        // TODO(F-05): 질의 이력
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
