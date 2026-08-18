package com.aiqa.pipeline;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Extracts raw text from an uploaded business requirement file so it can be handed to the
 * requirement splitter without the caller needing to know about file formats.
 *
 * <p>Supported formats: {@code .txt}, {@code .md}, {@code .docx}, {@code .pdf}. Any other
 * extension is read as best-effort UTF-8 plain text.</p>
 */
@Component
public class RequirementFileExtractor {

    public String extract(MultipartFile file) throws IOException {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        try (InputStream in = file.getInputStream()) {
            if (name.endsWith(".docx")) {
                return extractDocx(in);
            }
            if (name.endsWith(".pdf")) {
                return extractPdf(in);
            }
            // .txt, .md and anything else: treat as plain text.
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String extractDocx(InputStream in) throws IOException {
        StringBuilder text = new StringBuilder();
        try (XWPFDocument document = new XWPFDocument(in)) {
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                text.append(paragraph.getText()).append("\n");
            }
        }
        return text.toString();
    }

    private String extractPdf(InputStream in) throws IOException {
        try (PDDocument document = PDDocument.load(in)) {
            return new PDFTextStripper().getText(document);
        }
    }
}
