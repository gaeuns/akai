package com.tadak.akai.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** 이용자: 문서 검색, 요약 조회, 질문, 이력 조회 */
@Entity
@DiscriminatorValue("MEMBER")
public class Member extends User {

    protected Member() {
    }

    public Member(String loginId, String passwordHash, String name) {
        super(loginId, passwordHash, name);
    }

    @Override
    public boolean canManageDocuments() {
        return false;
    }
}
