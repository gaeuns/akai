package com.tadak.akai.extractor;

import com.tadak.akai.domain.FileType;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

/** Adapter: PDFBox를 TextExtractor 인터페이스로 감싼다. 페이지마다 텍스트를 따로 뽑는다. */
@Component
public class PdfTextExtractor implements TextExtractor {

    @Override
    public FileType supportedType() {
        return FileType.PDF;
    }

    @Override
    public List<PageText> extract(Path file) throws IOException {
        try (PDDocument pdf = Loader.loadPDF(file.toFile())) {
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
}
