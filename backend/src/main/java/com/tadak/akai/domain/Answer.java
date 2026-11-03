package com.tadak.akai.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/** 답변. 근거 조각을 가리키고, 근거가 없으면 found=false로 저장한다. */
@Entity
@Table(name = "answers")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", unique = true)
    private Question question;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean found;

    @ManyToMany
    @JoinTable(
            name = "answer_evidences",
            joinColumns = @JoinColumn(name = "answer_id"),
            inverseJoinColumns = @JoinColumn(name = "chunk_id"))
    private List<Chunk> evidences = new ArrayList<>();

    protected Answer() {
    }

    public Answer(Question question, String content, boolean found, List<Chunk> evidences) {
        this.question = question;
        this.content = content;
        this.found = found;
        this.evidences = new ArrayList<>(evidences);
    }

    public Long getId() {
        return id;
    }

    public Question getQuestion() {
        return question;
    }

    public String getContent() {
        return content;
    }

    public boolean isFound() {
        return found;
    }

    public List<Chunk> getEvidences() {
        return evidences;
    }
}
