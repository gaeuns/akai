package com.tadak.akai.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 문서. 조각·요약은 문서와 함께 삭제되고(구성), 태그·분류는 남는다(집합). */
@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType fileType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Chunk> chunks = new ArrayList<>();

    @OneToOne(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private Summary summary;

    @ManyToMany
    @JoinTable(
            name = "document_tags",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    protected Document() {
    }

    public Document(String title, String fileName, FileType fileType, User uploadedBy) {
        this.title = title;
        this.fileName = fileName;
        this.fileType = fileType;
        this.uploadedBy = uploadedBy;
        this.status = DocumentStatus.UPLOADED;
        this.createdAt = LocalDateTime.now();
    }

    public void addChunk(Chunk chunk) {
        chunks.add(chunk);
    }

    public void changeStatus(DocumentStatus status) {
        this.status = status;
    }

    public void assignSummary(Summary summary) {
        this.summary = summary;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getFileName() {
        return fileName;
    }

    public FileType getFileType() {
        return fileType;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public Category getCategory() {
        return category;
    }

    public User getUploadedBy() {
        return uploadedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<Chunk> getChunks() {
        return chunks;
    }

    public Summary getSummary() {
        return summary;
    }

    public Set<Tag> getTags() {
        return tags;
    }
}
