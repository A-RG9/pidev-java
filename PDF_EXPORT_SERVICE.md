# PdfExportService Implementation

## Overview
This implementation provides a `PdfExportService` class that exports health journal data to professional PDF reports using the iText library.

## Features Implemented

### 1. PDF Export Functionality
- **Main Method**: `exportHealthReport(Healthjournal healthjournal, String outputPath)`
  - Exports complete health journal to PDF
  - Handles null/empty data safely
  - Saves file to user-specified path

### 2. PDF Structure
The generated PDF includes:

#### Section 1: Title
- Main title: "Health Report"
- Journal name (if available)
- Date range (start to end date)

#### Section 2: Summary
- Health score (from journal ID as placeholder)
- Average weight (calculated from entries)
- Average glycemia (calculated from entries)
- Average sleep hours (calculated from entries)

#### Section 3: Daily Entries (Table Format)
- Date
- Weight (kg)
- Glycemia (g/l)
- Sleep (hours)
- Blood Pressure (mmHg)
- Number of symptoms

#### Section 4: Symptoms Overview
- Symptom type
- Occurrence count
- Sorted by frequency (most common first)

#### Section 5: Recommendations
- Personalized recommendations based on data
- Fallback message when no data available

#### Section 6: Footer
- Generation date
- System identifier

### 3. Technical Implementation

#### Dependencies
- **iText Library**: Used for PDF generation
  - `com.itextpdf:text` for document creation
  - `com.itextpdf:pdf` for PDF writing
  - `com.itextpdf:layout` for layout management

#### Key Classes
- `PdfExportService`: Main service class
- `Statistics`: Helper class for calculating averages

#### Safety Features
- Null checks for all input parameters
- Empty collection handling
- Data validation (weight range, glycemia range, sleep range)
- Try-finally block for proper document closure

### 4. Code Structure

```java
public class PdfExportService {
    // Main export method
    public void exportHealthReport(Healthjournal healthjournal, String outputPath) 
            throws IOException, DocumentException
    
    // Helper methods
    private Statistics calculateStatistics(List<Healthentry> entries)
    private void addTitleSection(Document document, Healthjournal journal)
    private void addSummarySection(Document document, Statistics stats, Healthjournal journal)
    private void addEntriesSection(Document document, List<Healthentry> entries)
    private void addSymptomsSection(Document document, List<Healthentry> entries)
    private void addRecommendationsSection(Document document, Healthjournal journal)
    private void addPdfFooter(StringBuilder pdf)
    
    // Internal helper class
    private static class Statistics {
        double averageWeight = 0;
        double averageGlycemia = 0;
        double averageSleep = 0;
    }
}
```

### 5. Data Handling

#### Entry Validation
- Weight: Must be between 0 and 200 kg
- Glycemia: Must be between 0.5 and 3 g/l
- Sleep: Must be between 0 and 12 hours

#### Symptom Processing
- Groups symptoms by type
- Counts occurrences
- Sorts by frequency

### 6. Usage Example

```java
// Create health journal with entries
Healthjournal journal = new Healthjournal();
journal.setId(1);
journal.setName("My Health Journal");
journal.setDatedebut(LocalDate.of(2026, 1, 1));
journal.setDatefin(LocalDate.of(2026, 1, 31));

// Add entries with symptoms
List<Healthentry> entries = new ArrayList<>();
Healthentry entry = new Healthentry();
entry.setDate(LocalDate.now());
entry.setPoids(70.5);
entry.setGlycemie(1.2);
entry.setSommeil(8);
entry.setTension("120/80");

// Add symptoms
List<Symptom> symptoms = new ArrayList<>();
Symptom symptom = new Symptom();
symptom.setType("Fatigue");
symptoms.add(symptom);
entry.setSymptoms(symptoms);

entries.add(entry);
journal.setEntries(entries);

// Export to PDF
PdfExportService exporter = new PdfExportService();
exporter.exportHealthReport(journal, "health_report.pdf");
```

### 7. Testing
A test class is provided in `src/test/java/com/wellora/services/PdfExportServiceTest.java` that demonstrates:
- Creating a health journal with multiple entries
- Adding symptoms to entries
- Exporting to PDF
- Verifying the export process

### 8. Future Enhancements
- Add more sophisticated health score calculation
- Include charts and graphs
- Support for multiple output formats (HTML, CSV)
- Email integration for automatic report sending
- Template system for custom report layouts