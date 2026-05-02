package com.wellora.services;

import com.wellora.model.Healthjournal;
import com.wellora.model.Healthentry;
import com.wellora.model.Symptom;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class for PdfExportService
 */
public class PdfExportServiceTest {
    
    public static void main(String[] args) {
        try {
            // Create a test health journal
            Healthjournal journal = new Healthjournal();
            journal.setId(1);
            journal.setName("My Health Journal");
            journal.setDatedebut(LocalDate.of(2026, 1, 1));
            journal.setDatefin(LocalDate.of(2026, 1, 31));
            
            // Create some test entries
            List<Healthentry> entries = new ArrayList<>();
            
            Healthentry entry1 = new Healthentry();
            entry1.setId(1);
            entry1.setDate(LocalDate.of(2026, 1, 15));
            entry1.setPoids(70.5);
            entry1.setGlycemie(1.2);
            entry1.setSommeil(8);
            entry1.setTension("120/80");
            
            // Add symptoms to entry
            List<Symptom> symptoms = new ArrayList<>();
            Symptom symptom1 = new Symptom();
            symptom1.setId(1);
            symptom1.setType("Fatigue");
            symptom1.setIntensite(5);
            symptoms.add(symptom1);
            entry1.setSymptoms(symptoms);
            
            entries.add(entry1);
            
            // Create another entry
            Healthentry entry2 = new Healthentry();
            entry2.setId(2);
            entry2.setDate(LocalDate.of(2026, 1, 20));
            entry2.setPoids(71.0);
            entry2.setGlycemie(1.1);
            entry2.setSommeil(7);
            entry2.setTension("118/78");
            entries.add(entry2);
            
            journal.setEntries(entries);
            
            // Create PdfExportService and export
            PdfExportService exporter = new PdfExportService();
            String outputPath = "health_report_test.pdf";
            
            System.out.println("Testing PdfExportService...");
            System.out.println("Journal: " + journal.getName());
            System.out.println("Entries: " + entries.size());
            
            exporter.exportHealthReport(journal, outputPath);
            
            System.out.println("PDF exported successfully to: " + outputPath);
            System.out.println("Test completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Test failed with exception:");
            e.printStackTrace();
        }
    }
}