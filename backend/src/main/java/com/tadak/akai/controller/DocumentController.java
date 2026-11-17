package com.tadak.akai.controller;

import com.tadak.akai.config.LoginUser;
import com.tadak.akai.dto.DocumentDetail;
import com.tadak.akai.dto.DocumentListItem;
import com.tadak.akai.service.DocumentService;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 업로드·삭제는 관리자만 호출할 수 있다(SecurityConfig에서 검사). */
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public List<DocumentListItem> list(@RequestParam(value = "keyword", required = false) String keyword) {
        return documentService.search(keyword);
    }

    @GetMapping("/{id}")
    public DocumentDetail detail(@PathVariable("id") Long id) {
        return documentService.findById(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentDetail> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @AuthenticationPrincipal LoginUser loginUser) throws IOException {
        DocumentDetail created = documentService.upload(file, title, loginUser.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) throws IOException {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
