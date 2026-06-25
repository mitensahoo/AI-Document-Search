package com.example.rag.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfService {

    public String extractText(InputStream pdfStream) throws Exception {
        try (PDDocument document = PDDocument.load(pdfStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    public List<String> chunkText(String text, int maxChars) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) return chunks;

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(text.length(), start + maxChars);
            // try to break at last newline or space for nicer chunks
            if (end < text.length()) {
                int lastNewline = text.lastIndexOf('\n', end);
                int lastSpace = text.lastIndexOf(' ', end);
                int split = Math.max(lastNewline, lastSpace);
                if (split > start) end = split;
            }
            chunks.add(text.substring(start, end).trim());
            start = end;
        }
        return chunks;
    }
}
