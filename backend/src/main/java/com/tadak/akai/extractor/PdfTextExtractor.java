package com.tadak.akai.extractor;

import com.tadak.akai.domain.FileType;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

/**
 * Adapter: PDFBox를 TextExtractor 인터페이스로 감싼다. 페이지마다 텍스트를 따로 뽑는다.
 * 암호가 걸렸거나 손상된 PDF는 사용자가 고칠 수 있는 입력 오류이므로 IllegalArgumentException(400)으로 바꾼다.
 */
@Component
public class PdfTextExtractor implements TextExtractor {

    @Override
    public FileType supportedType() {
        return FileType.PDF;
    }

    @Override
    public List<PageText> extract(Path file) throws IOException {
        try (PDDocument pdf = loadPdf(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            List<PageText> pages = new ArrayList<>();
            for (int pageNo = 1; pageNo <= pdf.getNumberOfPages(); pageNo++) {
                stripper.setStartPage(pageNo);
                stripper.setEndPage(pageNo);
                pages.add(new PageText(pageNo, stripper.getText(pdf)));
            }
            return pages;
        }
    }

    private PDDocument loadPdf(Path file) {
        try {
            return Loader.loadPDF(file.toFile());
        } catch (InvalidPasswordException e) {
            throw new IllegalArgumentException("암호가 걸린 PDF는 올릴 수 없습니다. 암호를 해제한 뒤 다시 올려 주세요.", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("PDF 파일을 읽을 수 없습니다. 손상된 파일인지 확인하세요.", e);
        }
    }
}
