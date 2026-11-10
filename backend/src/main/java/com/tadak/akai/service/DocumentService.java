package com.tadak.akai.service;

import com.tadak.akai.domain.Document;
import com.tadak.akai.domain.DocumentStatus;
import com.tadak.akai.domain.FileType;
import com.tadak.akai.domain.User;
import com.tadak.akai.dto.DocumentDetail;
import com.tadak.akai.dto.DocumentListItem;
import com.tadak.akai.pipeline.DocumentPipeline;
import com.tadak.akai.repository.DocumentRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentPipeline documentPipeline;
    private final Path uploadDir;

    public DocumentService(
            DocumentRepository documentRepository,
            DocumentPipeline documentPipeline,
            @Value("${app.upload-dir:./uploads}") String uploadDir) {
        this.documentRepository = documentRepository;
        this.documentPipeline = documentPipeline;
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath();
    }

    /** F-01: 형식 확인 → 원본 저장 → 추출·분할·요약 파이프라인 → 저장. 처리에 실패하면 문서는 저장되지 않는다. */
    @Transactional
    public DocumentDetail upload(MultipartFile file, String title, User uploader) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 올릴 수 없습니다.");
        }
        String fileName = file.getOriginalFilename();
        FileType fileType = FileType.fromFileName(fileName);
        String documentTitle = (title == null || title.isBlank()) ? removeExtension(fileName) : title.strip();

        Document document = new Document(documentTitle, fileName, fileType, uploader);
        document.changeStatus(DocumentStatus.PROCESSING);
        documentRepository.save(document);

        Path stored = store(file, document.getId(), fileType);
        documentPipeline.process(document, stored);
        return DocumentDetail.from(document);
    }

    /** F-04: 검색어가 없으면 전체 목록, 있으면 제목·키워드 검색 (최신순) */
    public List<DocumentListItem> search(String keyword) {
        List<Document> documents = (keyword == null || keyword.isBlank())
                ? documentRepository.findAllByOrderByCreatedAtDesc()
                : documentRepository.searchByTitleOrKeyword(keyword.strip());
        return documents.stream().map(DocumentListItem::from).toList();
    }

    public DocumentDetail findById(Long id) {
        return DocumentDetail.from(getDocument(id));
    }

    /** 문서를 지우면 조각·요약도 함께 지워진다(구성). TODO(F-08): 관리자 권한 검사 */
    @Transactional
    public void delete(Long id) throws IOException {
        Document document = getDocument(id);
        documentRepository.delete(document);
        Files.deleteIfExists(storedPath(id, document.getFileType()));
    }

    private Document getDocument(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("문서를 찾을 수 없습니다: " + id));
    }

    private Path store(MultipartFile file, Long id, FileType fileType) throws IOException {
        Files.createDirectories(uploadDir);
        Path target = storedPath(id, fileType);
        try (InputStream input = file.getInputStream()) {
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return target;
    }

    private Path storedPath(Long id, FileType fileType) {
        return uploadDir.resolve(id + "." + fileType.extension());
    }

    private String removeExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }
}
