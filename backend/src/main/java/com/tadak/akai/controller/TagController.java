package com.tadak.akai.controller;

import com.tadak.akai.dto.CategoryView;
import com.tadak.akai.dto.ClassificationRequest;
import com.tadak.akai.dto.DocumentDetail;
import com.tadak.akai.dto.NameRequest;
import com.tadak.akai.dto.TagView;
import com.tadak.akai.service.TagService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * F-04 · UC-03: 분류·태그 관리와 문서 분류.
 * 조회(GET)는 로그인한 사용자 모두, 추가·수정·삭제와 문서 분류는 관리자만 호출할 수 있다(SecurityConfig에서 검사).
 */
@RestController
@RequestMapping("/api")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/categories")
    public List<CategoryView> categories() {
        return tagService.findAllCategories();
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryView> createCategory(@RequestBody NameRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.createCategory(request.name()));
    }

    @PutMapping("/categories/{id}")
    public CategoryView renameCategory(@PathVariable("id") Long id, @RequestBody NameRequest request) {
        return tagService.renameCategory(id, request.name());
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") Long id) {
        tagService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tags")
    public List<TagView> tags() {
        return tagService.findAllTags();
    }

    @PostMapping("/tags")
    public ResponseEntity<TagView> createTag(@RequestBody NameRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.createTag(request.name()));
    }

    @PutMapping("/tags/{id}")
    public TagView renameTag(@PathVariable("id") Long id, @RequestBody NameRequest request) {
        return tagService.renameTag(id, request.name());
    }

    @DeleteMapping("/tags/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable("id") Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/documents/{id}/classification")
    public DocumentDetail classify(@PathVariable("id") Long id, @RequestBody ClassificationRequest request) {
        return tagService.classify(id, request.categoryId(), request.tagIds());
    }
}
