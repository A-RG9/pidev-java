package com.wellora.controllers;

import com.wellora.dao.FoodLogDAO;
import com.wellora.models.FoodLog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import javafx.scene.layout.BorderPane;

public class JournalController {

    @FXML private BorderPane rootPane;
    @FXML private ToggleButton btnThemeToggle;
    @FXML private DatePicker datePicker;
    @FXML private Label lblCalories, lblProteines, lblGlucides, lblLipides;
    @FXML private TableView<FoodLog> tableHistorique;
    @FXML private TableColumn<FoodLog, String> colRepas;
    @FXML private TableColumn<FoodLog, Integer> colCalories;
    @FXML private TableColumn<FoodLog, Double> colProteines, colGlucides, colLipides;

    private final FoodLogDAO dao = new FoodLogDAO();
    private final String currentUserUuid = "c513e200-d605-4f61-9efc-d539f0e80914";
    private ObservableList<FoodLog> foodLogsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Lier les colonnes
        colRepas.setCellValueFactory(new PropertyValueFactory<>("mealType"));
        colCalories.setCellValueFactory(new PropertyValueFactory<>("totalCalories"));
        colProteines.setCellValueFactory(new PropertyValueFactory<>("totalProtein"));
        colGlucides.setCellValueFactory(new PropertyValueFactory<>("totalCarbs"));
        colLipides.setCellValueFactory(new PropertyValueFactory<>("totalFats"));

        tableHistorique.setItems(foodLogsList);

        // Configurer le DatePicker par défaut sur aujourd'hui
        datePicker.setValue(LocalDate.now());

        // Écouter les changements de date
        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                loadHistoryForDate(newDate);
            }
        });

        // Charger les données initiales
        loadHistoryForDate(LocalDate.now());
    }

    private void loadHistoryForDate(LocalDate date) {
        double[] totals = dao.getDailyTotals(currentUserUuid, date);
        lblCalories.setText(String.format("%.0f kcal", totals[0]));
        lblProteines.setText(String.format("%.0fg", totals[1]));
        lblGlucides.setText(String.format("%.0fg", totals[2]));
        lblLipides.setText(String.format("%.0fg", totals[3]));

        List<FoodLog> logs = dao.getFoodLogsByDate(currentUserUuid, date);
        foodLogsList.setAll(logs);
    }

    // --- NAVIGATION ---
    @FXML
    public void navToDashboard(ActionEvent event) {
        switchScene(event, "Dashboard.fxml");
    }

    @FXML
    public void navToJournal(ActionEvent event) {
        // Déjà sur la page Journal
    }

    private void switchScene(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/wellora/views/" + fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/com/wellora/css/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void toggleTheme() {
        if (btnThemeToggle.isSelected()) {
            rootPane.getStyleClass().add("light-theme");
            btnThemeToggle.setText("☀️ Mode Clair");
        } else {
            rootPane.getStyleClass().remove("light-theme");
            btnThemeToggle.setText("🌙 Mode Sombre");
        }
    }
}
