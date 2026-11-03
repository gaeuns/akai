package com.tadak.akai.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** 관리자: 이용자 기능 + 업로드·분류·태그·삭제 */
@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User {

    protected Admin() {
    }

    public Admin(String loginId, String passwordHash, String name) {
        super(loginId, passwordHash, name);
    }

    @Override
    public boolean canManageDocuments() {
        return true;
    }
}
