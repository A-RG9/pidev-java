package com.wellora.services;

import com.itextpdf.layout.properties.BorderRadius;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.wellora.models.DailyPlan;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.Chart;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfExportService {

    public void exportPlanToPdf(Stage stage, Chart progressChart, List<DailyPlan> weeklyPlan) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder le Rapport WellCare");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("WellCare_Rapport_7Jours.pdf");

        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                PdfWriter writer = new PdfWriter(file.getAbsolutePath());
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);
                document.setMargins(40, 40, 40, 40);

                Color emeraldGreen = new DeviceRgb(0, 230, 164);
                Color darkNavy = new DeviceRgb(42, 47, 61);
                Color lightGrayRow = new DeviceRgb(243, 244, 246);
                Color textColor = new DeviceRgb(17, 24, 39);
                Color grayText = new DeviceRgb(107, 114, 128);

                // --- EN-TÊTE ---
                Table headerTable = new Table(1).useAllAvailableWidth();
                Cell headerCell = new Cell().add(new Paragraph("♥ WellCare")
                                .setFontSize(26).setBold().setFontColor(textColor))
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(emeraldGreen, 3f));
                headerTable.addCell(headerCell);
                document.add(headerTable);

                document.add(new Paragraph("Rapport des 7 derniers jours généré le " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                        .setFontColor(grayText).setFontSize(10).setMarginBottom(20));

                // --- GRAPHIQUE (Ignoré si null) ---
                if (progressChart != null) {
                    document.add(new Paragraph("📊 Analyse de vos Progrès")
                            .setBackgroundColor(darkNavy).setFontColor(ColorConstants.WHITE)
                            .setBold().setFontSize(14).setPadding(8).setBorderRadius(new BorderRadius(4f)));

                    WritableImage snapshot = progressChart.snapshot(new SnapshotParameters(), null);
                    ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
                    ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", byteOutput);
                    ImageData imageData = ImageDataFactory.create(byteOutput.toByteArray());
                    Image pdfImage = new Image(imageData);
                    pdfImage.setAutoScale(true);
                    document.add(pdfImage.setMarginBottom(20));
                }

                // --- TABLEAU DES 7 JOURS ---
                document.add(new Paragraph("📅 Planificateur (Données Réelles)")
                        .setBackgroundColor(darkNavy).setFontColor(ColorConstants.WHITE)
                        .setBold().setFontSize(14).setPadding(8).setBorderRadius(new BorderRadius(4f)).setMarginTop(10));

                float[] columnWidths = {2.5f, 2.5f, 2.5f, 2.5f, 2, 1.5f};
                Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();

                String[] headers = {"Date", "Petit-Déj", "Déjeuner", "Dîner", "Snack", "Total"};
                for (String header : headers) {
                    table.addHeaderCell(new Cell().add(new Paragraph(header).setBold())
                            .setBackgroundColor(emeraldGreen).setFontColor(textColor)
                            .setBorder(Border.NO_BORDER).setPadding(8));
                }

                // On injecte les données récupérées depuis la BDD via le Contrôleur
                int rowIndex = 0;
                for (DailyPlan day : weeklyPlan) {
                    boolean isEven = (rowIndex % 2 == 0);
                    Color bgColor = isEven ? lightGrayRow : ColorConstants.WHITE;

                    addStyledCell(table, day.dateString, bgColor, textColor, true);
                    addStyledCell(table, day.breakfast, bgColor, grayText, false);
                    addStyledCell(table, day.lunch, bgColor, grayText, false);
                    addStyledCell(table, day.dinner, bgColor, grayText, false);
                    addStyledCell(table, day.snack, bgColor, grayText, false);
                    addStyledCell(table, day.totalCalories + " kcal", bgColor, emeraldGreen, true);

                    rowIndex++;
                }

                document.add(table);

                // --- PIED DE PAGE ---
                document.add(new Paragraph("\n© 2026 WellCare - Restez en bonne santé !")
                        .setTextAlignment(TextAlignment.CENTER).setFontColor(grayText).setFontSize(9));

                document.close();
                System.out.println("✅ PDF généré avec les vraies données !");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addStyledCell(Table table, String text, Color bgColor, Color fontColor, boolean isBold) {
        Paragraph p = new Paragraph(text != null && !text.isEmpty() ? text : "-").setFontColor(fontColor).setFontSize(10);
        if (isBold) p.setBold();
        table.addCell(new Cell().add(p).setBackgroundColor(bgColor).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(new DeviceRgb(229, 231, 235), 1f)).setPadding(8));
    }
}