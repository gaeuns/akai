package com.tadak.akai.dto;

import com.tadak.akai.domain.Answer;
import com.tadak.akai.domain.Question;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 질문 하나와 답변·근거. found가 false면 "문서에서 찾을 수 없음"이고 근거는 비어 있다.
 * aiGenerated는 AI가 만든 답변일 때만 true다 (고정 문구에는 "AI 생성" 표시를 붙이지 않는다).
 * 근거 문서가 삭제되면 found는 true인데 evidences가 비어 있을 수 있다 (질의 이력).
 */
public record AnswerView(
        Long questionId,
        String question,
        String answer,
        boolean found,
        boolean aiGenerated,
        LocalDateTime askedAt,
        List<EvidenceView> evidences) {

    public static AnswerView from(Question question) {
        Answer answer = question.getAnswer();
        return new AnswerView(
                question.getId(),
                question.getContent(),
                answer.getContent(),
                answer.isFound(),
                answer.isFound(),
                question.getAskedAt(),
                answer.getEvidences().stream().map(EvidenceView::from).toList());
    }
}
