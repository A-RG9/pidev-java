package com.wellora.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import com.wellora.model.Healthjournal;
import com.wellora.model.Healthentry;
import com.wellora.model.Symptom;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PdfExportService {

    public void exportHealthReport(Healthjournal journal, String outputPath)
            throws IOException, DocumentException {

        if (journal == null) {
            throw new IllegalArgumentException("Healthjournal cannot be null");
        }

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(outputPath));
        document.open();

        try {
            List<Healthentry> entries = journal.getEntriesByDateRange();

            addTitleSection(document, journal);
            addSummarySection(document, entries);
            addEntriesSection(document, entries);
            addSymptomsSection(document, entries);
            addRecommendationsSection(document, entries);

        } finally {
            document.close();
        }
    }

    // ================= TITLE =================
    private void addTitleSection(Document document, Healthjournal journal) throws DocumentException {
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph title = new Paragraph("HEALTH REPORT\n\n", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        if (journal.getName() != null) {
            document.add(new Paragraph("Journal: " + journal.getName()));
        }

        if (journal.getDatedebut() != null && journal.getDatefin() != null) {
            document.add(new Paragraph("Period: "
                    + journal.getDatedebut() + " to " + journal.getDatefin()));
        }

        document.add(new Paragraph("\n"));
    }

    // ================= SUMMARY =================
    private void addSummarySection(Document document, List<Healthentry> entries)
            throws DocumentException {

        document.add(new Paragraph("SUMMARY\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
        document.add(new Paragraph(" "));

        Statistics stats = calculateStatistics(entries);

        document.add(new Paragraph(String.format("Average Weight: %.2f kg", stats.avgWeight)));
        document.add(new Paragraph(String.format("Average Glycemia: %.2f g/l", stats.avgGlycemia)));
        document.add(new Paragraph(String.format("Average Sleep: %.2f hours", stats.avgSleep)));

        document.add(new Paragraph("\n"));
    }

    // ================= ENTRIES TABLE =================
    private void addEntriesSection(Document document, List<Healthentry> entries)
            throws DocumentException {

        document.add(new Paragraph("DAILY ENTRIES\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
        document.add(new Paragraph(" "));

        if (entries == null || entries.isEmpty()) {
            document.add(new Paragraph("No entries available.\n"));
            return;
        }

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);

        addTableHeader(table);

        for (Healthentry e : entries) {
            table.addCell(value(e.getDate()));
            table.addCell(value(e.getPoids()));
            table.addCell(value(e.getGlycemie()));
            table.addCell(value(e.getSommeil()));
            table.addCell(value(e.getTension()));
            table.addCell(e.getSymptoms() != null ? String.valueOf(e.getSymptoms().size()) : "0");
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addTableHeader(PdfPTable table) {
        table.addCell("Date");
        table.addCell("Weight");
        table.addCell("Glycemia");
        table.addCell("Sleep");
        table.addCell("BP");
        table.addCell("Symptoms");
    }

    // ================= SYMPTOMS =================
    private void addSymptomsSection(Document document, List<Healthentry> entries)
            throws DocumentException {

        document.add(new Paragraph("SYMPTOMS OVERVIEW\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
        document.add(new Paragraph(" "));

        if (entries == null || entries.isEmpty()) {
            document.add(new Paragraph("No symptoms recorded.\n"));
            return;
        }

        Map<String, Integer> count = new HashMap<>();

        for (Healthentry e : entries) {
            if (e.getSymptoms() != null) {
                for (Symptom s : e.getSymptoms()) {
                    String type = s.getType() != null ? s.getType() : "Unknown";
                    count.put(type, count.getOrDefault(type, 0) + 1);
                }
            }
        }

        if (count.isEmpty()) {
            document.add(new Paragraph("No symptoms recorded.\n"));
            return;
        }

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);

        table.addCell("Symptom");
        table.addCell("Occurrences");

        for (Map.Entry<String, Integer> entry : count.entrySet()) {
            table.addCell(entry.getKey());
            table.addCell(String.valueOf(entry.getValue()));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    // ================= RECOMMENDATIONS =================
    private void addRecommendationsSection(Document document, List<Healthentry> entries)
            throws DocumentException {

        document.add(new Paragraph("RECOMMENDATIONS\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
        document.add(new Paragraph(" "));

        if (entries == null || entries.isEmpty()) {
            document.add(new Paragraph("No data available for recommendations.\n"));
            return;
        }

        document.add(new Paragraph("1. Maintain regular sleep patterns"));
        document.add(new Paragraph("2. Monitor blood sugar regularly"));
        document.add(new Paragraph("3. Keep tracking your weight"));
        document.add(new Paragraph("4. Stay hydrated"));
        document.add(new Paragraph("5. Consult your doctor if symptoms persist"));

        document.add(new Paragraph("\n"));
    }

    // ================= UTIL =================
    private String value(Object obj) {
        return obj != null ? obj.toString() : "N/A";
    }

    // ================= STATS =================
    private Statistics calculateStatistics(List<Healthentry> entries) {
        Statistics stats = new Statistics();

        if (entries == null || entries.isEmpty()) {
            return stats;
        }

        double totalWeight = 0;
        double totalGlycemia = 0;
        double totalSleep = 0;
        int count = 0;

        for (Healthentry e : entries) {
            totalWeight += e.getPoids();
            totalGlycemia += e.getGlycemie();
            totalSleep += e.getSommeil();
            count++;
        }

        stats.avgWeight = totalWeight / count;
        stats.avgGlycemia = totalGlycemia / count;
        stats.avgSleep = totalSleep / count;

        return stats;
    }

    private static class Statistics {
        double avgWeight = 0;
        double avgGlycemia = 0;
        double avgSleep = 0;
    }
}