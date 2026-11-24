package com.tadak.akai.service;

import com.tadak.akai.dto.StatisticsView;
import com.tadak.akai.dto.StatisticsView.DocumentUsage;
import com.tadak.akai.dto.StatisticsView.RecentQuestion;
import com.tadak.akai.repository.ChunkRepository;
import com.tadak.akai.repository.DocumentRepository;
import com.tadak.akai.repository.QuestionRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** F-09: 저장된 질문·답변으로 품질 지표를 계산한다(관리자). AI는 쓰지 않는다. */
@Service
@Transactional(readOnly = true)
public class StatisticsService {

    /** NF-02 질문 응답 목표 */
    static final long RESPONSE_TARGET_MILLIS = 5000;
    static final int TOP_DOCUMENTS = 5;

    private final DocumentRepository documentRepository;
    private final ChunkRepository chunkRepository;
    private final QuestionRepository questionRepository;
    private final String searchStrategy;

    public StatisticsService(
            DocumentRepository documentRepository,
            ChunkRepository chunkRepository,
            QuestionRepository questionRepository,
            @Value("${app.search.strategy:keyword}") String searchStrategy) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.questionRepository = questionRepository;
        this.searchStrategy = searchStrategy;
    }

    public StatisticsView statistics() {
        long questionCount = questionRepository.count();
        long notFoundCount = questionRepository.countNotFound();
        long timedCount = questionRepository.countTimed();
        Double averageMillis = questionRepository.averageResponseMillis();

        return new StatisticsView(
                searchStrategy.strip(),
                documentRepository.count(),
                chunkRepository.count(),
                chunkRepository.countByEmbeddingIsNotNull(),
                questionCount,
                notFoundCount,
                percent(notFoundCount, questionCount),
                averageMillis == null ? null : averageMillis / 1000,
                percent(questionRepository.countRespondedWithin(RESPONSE_TARGET_MILLIS), timedCount),
                topDocuments(),
                recentNotFound());
    }

    private List<DocumentUsage> topDocuments() {
        return questionRepository.countEvidenceByDocument(PageRequest.of(0, TOP_DOCUMENTS)).stream()
                .map(row -> new DocumentUsage((Long) row[0], (String) row[1], (Long) row[2]))
                .toList();
    }

    private List<RecentQuestion> recentNotFound() {
        return questionRepository.findTop5ByAnswerFoundFalseOrderByAskedAtDescIdDesc().stream()
                .map(question -> new RecentQuestion(question.getContent(), question.getAskedAt()))
                .toList();
    }

    private Double percent(long part, long total) {
        return total == 0 ? null : part * 100.0 / total;
    }
}
