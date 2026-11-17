package com.tadak.akai.dto;

public record LoginRequest(String loginId, String password) {

    public boolean isBlank() {
        return loginId == null || loginId.isBlank() || password == null || password.isEmpty();
    }
}
