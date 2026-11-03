package com.tadak.akai.service;

import com.tadak.akai.repository.TagRepository;
import org.springframework.stereotype.Service;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    // TODO(F-04): 분류·태그 추가·수정, 문서에 태그 붙이기
}
