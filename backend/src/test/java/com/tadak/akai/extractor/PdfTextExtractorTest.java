package com.tadak.akai.extractor;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PdfTextExtractorTest {

    private final PdfTextExtractor extractor = new PdfTextExtractor();

    @Test
    void 텍스트_PDF는_페이지마다_번호를_붙여_추출한다(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("rule.pdf");
        writePdf(file, "Article 1 Purpose", "Article 2 Semester");

        List<PageText> pages = extractor.extract(file);

        assertThat(pages).extracting(PageText::pageNo).containsExactly(1, 2);
        assertThat(pages.get(0).text()).contains("Article 1 Purpose");
        assertThat(pages.get(1).text()).contains("Article 2 Semester");
    }

    private void writePdf(Path file, String... pageTexts) throws Exception {
        try (PDDocument document = new PDDocument()) {
            for (String text : pageTexts) {
                PDPage page = new PDPage();
                document.addPage(page);
                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    content.beginText();
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    content.newLineAtOffset(72, 700);
                    content.showText(text);
                    content.endText();
                }
            }
            document.save(file.toFile());
        }
    }
}
