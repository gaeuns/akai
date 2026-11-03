package com.tadak.akai.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** 문서 요약과 키워드. 업로드 때 한 번만 생성하고, 관리자가 수정할 수 있다. */
@Entity
@Table(name = "summaries")
public class Summary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", unique = true)
    private Document document;

    @Lob
    @Column(nullable = false)
    private String content;

    /** 쉼표로 구분한 핵심 키워드 5개 */
    private String keywords;

    @Column(nullable = false)
    private boolean aiGenerated;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Summary() {
    }

    public Summary(Document document, String content, String keywords) {
        this.document = document;
        this.content = content;
        this.keywords = keywords;
        this.aiGenerated = true;
        this.updatedAt = LocalDateTime.now();
    }

    /** 관리자가 수정하면 AI 생성 표시를 끈다 */
    public void editByAdmin(String content, String keywords) {
        this.content = content;
        this.keywords = keywords;
        this.aiGenerated = false;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Document getDocument() {
        return document;
    }

    public String getContent() {
        return content;
    }

    public String getKeywords() {
        return keywords;
    }

    public boolean isAiGenerated() {
        return aiGenerated;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
