package com.tadak.akai.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime askedAt;

    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private Answer answer;

    /** F-09: 검색부터 답변까지 걸린 시간(ms). 기능 추가 전에 저장한 질문은 null */
    private Long responseMillis;

    protected Question() {
    }

    public Question(User user, String content) {
        this.user = user;
        this.content = content;
        this.askedAt = LocalDateTime.now();
    }

    public void assignAnswer(Answer answer) {
        this.answer = answer;
    }

    public void recordResponseTime(long millis) {
        this.responseMillis = millis;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getAskedAt() {
        return askedAt;
    }

    public Answer getAnswer() {
        return answer;
    }

    public Long getResponseMillis() {
        return responseMillis;
    }
}
