package com.tadak.akai.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** 문서의 페이지·문단 단위 조각. 답변의 근거가 된다. */
@Entity
@Table(name = "chunks")
public class Chunk {

    public static final int MAX_CONTENT_LENGTH = 10000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id")
    private Document document;

    @Column(nullable = false)
    private int pageNo;

    @Column(nullable = false)
    private int seq;

    /** 키워드 검색(대소문자 무시)을 하려면 CLOB이 아닌 VARCHAR여야 한다. 조각 길이는 ChunkSplitter가 이 안으로 자른다. */
    @Column(nullable = false, length = MAX_CONTENT_LENGTH)
    private String content;

    protected Chunk() {
    }

    public Chunk(Document document, int pageNo, int seq, String content) {
        this.document = document;
        this.pageNo = pageNo;
        this.seq = seq;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public Document getDocument() {
        return document;
    }

    public int getPageNo() {
        return pageNo;
    }

    public int getSeq() {
        return seq;
    }

    public String getContent() {
        return content;
    }
}
