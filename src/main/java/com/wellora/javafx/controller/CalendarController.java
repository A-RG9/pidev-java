package com.wellora.javafx.controller;

import com.wellora.controllers.HealthNavigationProxy;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.HPos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

import com.wellora.dao.HealthentryDAO;
import com.wellora.dao.SymptomDAO;
import com.wellora.services.ApiService;
import com.wellora.model.Healthentry;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

/**
 * CalendarController - Handles the Calendar view with health data and external API integration
 */
public class CalendarController {

    private HealthNavigationProxy proxy;

    // =========================
    // 🔧 FIELDS
    // =========================

    private MainController mainController;

    private YearMonth currentYearMonth;
    private LocalDate selectedDate;

    // DAOs
    private final HealthentryDAO entryDAO = new HealthentryDAO();
    private final SymptomDAO symptomDAO = new SymptomDAO();
    private final ApiService apiService = new ApiService();

    // FXML fields
    @FXML private Label monthYearLabel;
    @FXML private GridPane calendarGrid;
    @FXML private Label selectedDayLabel;
    @FXML private Label healthScoreLabel;
    @FXML private Label symptomsLabel;
    @FXML private Label temperatureLabel;
    @FXML private Label conditionsLabel;
    @FXML private Label weatherIcon;
    @FXML private VBox detailsPanel;

    // =========================
    // 🚀 INITIALIZE
    // =========================

    @FXML
    public void initialize() {
        currentYearMonth = YearMonth.now();
        generateCalendar();
    }

    // =========================
    // 🎯 SET MAIN CONTROLLER
    // =========================

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // =========================
    // 📅 CALENDAR GENERATION
    // =========================

    /**
     * Generate the calendar grid for the current month
     */
    private void generateCalendar() {
        // Clear previous entries
        calendarGrid.getChildren().clear();

        // Update month/year label
        String monthName = currentYearMonth.getMonth()
                .getDisplayName(TextStyle.FULL, Locale.FRENCH);
        monthYearLabel.setText(monthName.substring(0, 1).toUpperCase() 
                + monthName.substring(1) + " " + currentYearMonth.getYear());

        // Get first day of month and days in month
        LocalDate firstDay = currentYearMonth.atDay(1);
        int daysInMonth = currentYearMonth.lengthOfMonth();

        // Calculate starting position (Monday = 1, Sunday = 7)
        int startDayOfWeek = firstDay.getDayOfWeek().getValue();

        // Create calendar cells (6 rows maximum for any month)
        int row = 0;
        int col = startDayOfWeek - 1;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            VBox cell = createCalendarCell(date, day);

            calendarGrid.add(cell, col, row);

            // Move to next position
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    /**
     * Create a calendar cell for a specific date
     */
    private VBox createCalendarCell(LocalDate date, int dayNumber) {
        // Create cell container
        VBox cell = new VBox();
        cell.setAlignment(Pos.CENTER);
        cell.setMinSize(80, 80);
        cell.setMaxSize(80, 80);
        cell.setSpacing(2);
        cell.setCursor(Cursor.HAND);

        // Style based on date
        boolean isToday = date.equals(LocalDate.now());
        boolean hasData = hasHealthData(date);

        // Add CSS class
        String styleClass = "calendar-cell";
        if (isToday) {
            styleClass += " today-cell";
        } else if (hasData) {
            int score = getHealthScoreForDate(date);
            if (score >= 70) {
                styleClass += " good-cell";
            } else if (score >= 40) {
                styleClass += " medium-cell";
            } else {
                styleClass += " bad-cell";
            }
        }
        cell.setId(styleClass);

        // Day number label
        Label dayLabel = new Label(String.valueOf(dayNumber));
        dayLabel.getStyleClass().add("day-number");

        // Add score indicator circle if data exists
        if (hasData) {
            int score = getHealthScoreForDate(date);
            
            // Add colored circle based on score
            Circle scoreCircle = new Circle(12);
            if (score >= 70) {
                scoreCircle.setFill(Color.web("#22c55e")); // Green
            } else if (score >= 40) {
                scoreCircle.setFill(Color.web("#f59e0b")); // Orange
            } else {
                scoreCircle.setFill(Color.web("#ef4444")); // Red
            }
            
            // Add day number and score circle
            cell.getChildren().addAll(dayLabel, scoreCircle);

            // Set tooltip using Tooltip.install (VBox doesn't have setTooltip)
            Tooltip tooltip = new Tooltip(
                "Score: " + score + "\n" +
                "Symptômes: " + getSymptomCountForDate(date)
            );
            Tooltip.install(cell, tooltip);
        } else {
            cell.getChildren().add(dayLabel);
        }

        // Add click handler
        cell.setOnMouseClicked(e -> onDayClicked(date));

        // Add hover effect
        cell.setOnMouseEntered(e -> {
            cell.setStyle(cell.getStyle() + "-fx-background-color: #E2E8F0;");
        });
        cell.setOnMouseExited(e -> {
            cell.setStyle(cell.getStyle().replace("-fx-background-color: #E2E8F0;", ""));
        });

        return cell;
    }

    // =========================
    // 🎯 DAY SELECTION
    // =========================

    /**
     * Handle day click - show details
     */
    private void onDayClicked(LocalDate date) {
        selectedDate = date;
        selectedDayLabel.setText("Détails: " + date.format(
                DateTimeFormatter.ofPattern("dd MMMM yyyy")));

        // Load health data
        loadHealthData(date);

        // Load weather data
        loadWeatherData(date);

        // Show details panel
        detailsPanel.setVisible(true);
    }

    /**
     * Load health data for selected date
     */
    private void loadHealthData(LocalDate date) {
        try {
            // Get entry for date
            List<Healthentry> entries = entryDAO.findByMonth(
                    date.getYear(), date.getMonthValue());

            Healthentry entry = entries.stream()
                    .filter(e -> e.getDate().equals(date))
                    .findFirst()
                    .orElse(null);

            if (entry != null) {
                // Calculate score for this day
                int score = calculateDailyScore(entry);
                healthScoreLabel.setText(String.valueOf(score));

                // Color code the score
                if (score >= 70) {
                    healthScoreLabel.setStyle("-fx-text-fill: #27ae60;");
                } else if (score >= 40) {
                    healthScoreLabel.setStyle("-fx-text-fill: #f39c12;");
                } else {
                    healthScoreLabel.setStyle("-fx-text-fill: #e74c3c;");
                }
            } else {
                healthScoreLabel.setText("N/A");
                healthScoreLabel.setStyle("-fx-text-fill: #64748B;");
            }

            // Get symptoms count
            int symptomCount = getSymptomCountForDate(date);
            symptomsLabel.setText(String.valueOf(symptomCount));

        } catch (Exception e) {
            e.printStackTrace();
            healthScoreLabel.setText("--");
            symptomsLabel.setText("--");
        }
    }

    /**
     * Load weather data from API
     */
    private void loadWeatherData(LocalDate date) {
        try {
            // Get weather from API (or use fallback)
            String weather = apiService.getWeatherByDate(date);
            String[] parts = weather.split("|");

            if (parts.length >= 2) {
                temperatureLabel.setText("Température: " + parts[0]);
                conditionsLabel.setText("Conditions: " + parts[1]);
                weatherIcon.setText(parts[2]);
            } else {
                temperatureLabel.setText("Température: --");
                conditionsLabel.setText("Conditions: --");
                weatherIcon.setText("🌤️");
            }
        } catch (Exception e) {
            temperatureLabel.setText("Température: --");
            conditionsLabel.setText("Conditions: --");
            weatherIcon.setText("🌤️");
        }
    }

    // =========================
    // 🔄 MONTH NAVIGATION
    // =========================

    @FXML
    private void previousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        generateCalendar();
    }

    @FXML
    private void nextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        generateCalendar();
    }

    // =========================
    // 🔧 HELPERS
    // =========================

    /**
     * Check if date has health data
     */
    private boolean hasHealthData(LocalDate date) {
        try {
            List<Healthentry> entries = entryDAO.findByMonth(date.getYear(), date.getMonthValue());
            return entries.stream().anyMatch(e -> e.getDate().equals(date));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get health score for date
     */
    private int getHealthScoreForDate(LocalDate date) {
        try {
            List<Healthentry> entries = entryDAO.findByMonth(date.getYear(), date.getMonthValue());
            return entries.stream()
                    .filter(e -> e.getDate().equals(date))
                    .findFirst()
                    .map(this::calculateDailyScore)
                    .orElse(0);
        } catch (Exception e) {
            // ignore
        }
        return 0;
    }

    /**
     * Get symptom count for date
     */
    private int getSymptomCountForDate(LocalDate date) {
        try {
            List<Healthentry> entries = entryDAO.findByMonth(date.getYear(), date.getMonthValue());
            return entries.stream()
                    .filter(e -> e.getDate().equals(date))
                    .findFirst()
                    .map(e -> e.getSymptoms() != null ? e.getSymptoms().size() : 0)
                    .orElse(0);
        } catch (Exception e) {
            // ignore
        }
        return 0;
    }

    /**
     * Calculate daily health score
     */
    private int calculateDailyScore(Healthentry entry) {
        int score = 100;

        // Weight penalty
        if (entry.getPoids() > 0) {
            if (entry.getPoids() < 40 || entry.getPoids() > 120) {
                score -= 20;
            } else if (entry.getPoids() < 50 || entry.getPoids() > 100) {
                score -= 10;
            }
        }

        // Sleep penalty
        if (entry.getSommeil() > 0) {
            if (entry.getSommeil() < 5 || entry.getSommeil() > 10) {
                score -= 15;
            } else if (entry.getSommeil() < 6 || entry.getSommeil() > 9) {
                score -= 10;
            }
        }

        // Glycemia penalty
        if (entry.getGlycemie() > 0) {
            if (entry.getGlycemie() > 1.4) {
                score -= 15;
            } else if (entry.getGlycemie() > 1.1) {
                score -= 10;
            }
        }

        return Math.max(0, Math.min(100, score));
    }

    public void setMainControllerProxy(HealthNavigationProxy proxy) {
        this.proxy = proxy;
    }

}