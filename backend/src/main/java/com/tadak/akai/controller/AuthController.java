package com.tadak.akai.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<Void> login() {
        // TODO(F-08): 미리 등록된 계정으로 로그인
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
