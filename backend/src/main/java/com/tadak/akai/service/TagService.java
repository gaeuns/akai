package com.tadak.akai.service;

import com.tadak.akai.domain.Category;
import com.tadak.akai.domain.Document;
import com.tadak.akai.domain.Tag;
import com.tadak.akai.dto.CategoryView;
import com.tadak.akai.dto.DocumentDetail;
import com.tadak.akai.dto.TagView;
import com.tadak.akai.repository.CategoryRepository;
import com.tadak.akai.repository.DocumentRepository;
import com.tadak.akai.repository.TagRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * F-04 · UC-03: 분류·태그 추가·수정·삭제와 문서에 분류·태그 붙이기.
 * 분류·태그는 문서와 집합 관계라, 분류·태그를 지워도 문서는 남고 연결만 끊긴다.
 * 관리자 권한은 SecurityConfig에서 검사한다.
 */
@Service
@Transactional(readOnly = true)
public class TagService {

    /** schema.sql의 categories.name·tags.name 길이(VARCHAR(50)) */
    static final int MAX_NAME_LENGTH = 50;

    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;
    private final DocumentRepository documentRepository;

    public TagService(
            TagRepository tagRepository,
            CategoryRepository categoryRepository,
            DocumentRepository documentRepository) {
        this.tagRepository = tagRepository;
        this.categoryRepository = categoryRepository;
        this.documentRepository = documentRepository;
    }

    // ---- 분류 ----

    public List<CategoryView> findAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc().stream().map(CategoryView::from).toList();
    }

    @Transactional
    public CategoryView createCategory(String name) {
        String validName = validateName(name, "분류");
        checkDuplicate(categoryRepository.findByName(validName).map(Category::getId), null, "분류", validName);
        return CategoryView.from(categoryRepository.save(new Category(validName)));
    }

    @Transactional
    public CategoryView renameCategory(Long id, String name) {
        Category category = getCategory(id);
        String validName = validateName(name, "분류");
        checkDuplicate(categoryRepository.findByName(validName).map(Category::getId), id, "분류", validName);
        category.rename(validName);
        return CategoryView.from(category);
    }

    /** 분류를 지우면 그 분류의 문서는 미분류가 된다. */
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategory(id);
        documentRepository.clearCategory(id);
        categoryRepository.delete(category);
    }

    // ---- 태그 ----

    public List<TagView> findAllTags() {
        return tagRepository.findAllByOrderByNameAsc().stream().map(TagView::from).toList();
    }

    @Transactional
    public TagView createTag(String name) {
        String validName = validateName(name, "태그");
        checkDuplicate(tagRepository.findByName(validName).map(Tag::getId), null, "태그", validName);
        return TagView.from(tagRepository.save(new Tag(validName)));
    }

    @Transactional
    public TagView renameTag(Long id, String name) {
        Tag tag = getTag(id);
        String validName = validateName(name, "태그");
        checkDuplicate(tagRepository.findByName(validName).map(Tag::getId), id, "태그", validName);
        tag.rename(validName);
        return TagView.from(tag);
    }

    /** 태그를 지우면 문서에서 그 태그만 떨어진다. */
    @Transactional
    public void deleteTag(Long id) {
        Tag tag = getTag(id);
        for (Document document : documentRepository.findAllByTagId(id)) {
            document.removeTag(tag);
        }
        tagRepository.delete(tag);
    }

    // ---- 문서 분류 ----

    /** 문서의 분류와 태그를 요청 값으로 바꾼다. categoryId가 null이면 미분류, tagIds가 비면 태그를 모두 뗀다. */
    @Transactional
    public DocumentDetail classify(Long documentId, Long categoryId, List<Long> tagIds) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NoSuchElementException("문서를 찾을 수 없습니다: " + documentId));
        Category category = categoryId == null ? null : getCategory(categoryId);
        Set<Tag> tags = new LinkedHashSet<>();
        if (tagIds != null) {
            for (Long tagId : tagIds) {
                if (tagId == null) {
                    throw new IllegalArgumentException("태그 ID가 비어 있습니다.");
                }
                tags.add(getTag(tagId));
            }
        }
        document.changeCategory(category);
        document.replaceTags(tags);
        return DocumentDetail.from(document);
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("분류를 찾을 수 없습니다: " + id));
    }

    private Tag getTag(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("태그를 찾을 수 없습니다: " + id));
    }

    private String validateName(String name, String kind) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(kind + " 이름을 입력하세요.");
        }
        String stripped = name.strip();
        if (stripped.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(kind + " 이름은 " + MAX_NAME_LENGTH + "자 이하로 입력하세요.");
        }
        return stripped;
    }

    /** 같은 이름이 이미 있으면 거부한다. 이름을 바꿀 때 자기 자신(selfId)과 같은 이름은 허용한다. */
    private void checkDuplicate(Optional<Long> existingId, Long selfId, String kind, String name) {
        if (existingId.isPresent() && !existingId.get().equals(selfId)) {
            throw new IllegalArgumentException("이미 있는 " + kind + "입니다: " + name);
        }
    }
}
