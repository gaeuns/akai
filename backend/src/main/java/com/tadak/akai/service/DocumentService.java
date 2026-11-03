package com.tadak.akai.service;

import com.tadak.akai.domain.Document;
import com.tadak.akai.domain.User;
import com.tadak.akai.pipeline.DocumentPipeline;
import com.tadak.akai.repository.DocumentRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentPipeline documentPipeline;

    public DocumentService(DocumentRepository documentRepository, DocumentPipeline documentPipeline) {
        this.documentRepository = documentRepository;
        this.documentPipeline = documentPipeline;
    }

    public Document upload(MultipartFile file, User uploader) {
        // TODO(F-01): 형식 확인, 원본 저장, 파이프라인 실행
        throw new UnsupportedOperationException("F-01 구현 예정");
    }

    public List<Document> search(String keyword) {
        // TODO(F-04): 제목·태그·키워드 검색
        throw new UnsupportedOperationException("F-04 구현 예정");
    }

    public Document findById(Long id) {
        // TODO(F-02): 상세 조회
        throw new UnsupportedOperationException("F-02 구현 예정");
    }

    public void delete(Long id, User user) {
        // TODO(UC-04): 관리자만 삭제
        throw new UnsupportedOperationException("UC-04 구현 예정");
    }
}
