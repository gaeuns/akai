package com.tadak.akai.dto;

import com.tadak.akai.domain.Category;

/** 분류 (F-04) */
public record CategoryView(Long id, String name) {

    public static CategoryView from(Category category) {
        return category == null ? null : new CategoryView(category.getId(), category.getName());
    }
}
