package com.wellora.controllers;

import com.wellora.dao.FoodLogDAO;
import com.wellora.models.FoodLog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
import java.util.Comparator;
import java.util.List;
import com.wellora.utils.ThemeManager;

public class JournalController extends BaseController {

    @FXML private BorderPane rootPane;
    @FXML private ToggleButton btnThemeToggle;
    @FXML private DatePicker datePicker;
    @FXML private Label lblCalories, lblProteines, lblGlucides, lblLipides;

    // Filtres
    @FXML private ComboBox<String> comboMealFilter;
    @FXML private ComboBox<String> comboSortCalories;

    @FXML private TableView<FoodLog> tableHistorique;
    @FXML private TableColumn<FoodLog, String> colRepas;
    @FXML private TableColumn<FoodLog, Integer> colCalories;
    @FXML private TableColumn<FoodLog, Double> colProteines, colGlucides, colLipides;

    private final FoodLogDAO dao = new FoodLogDAO();
    private final String currentUserUuid = "c513e200-d605-4f61-9efc-d539f0e80914";
    private ObservableList<FoodLog> foodLogsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (ThemeManager.isDarkMode) {
            btnThemeToggle.setSelected(true);
            btnThemeToggle.setText("☀️ Mode Clair");
            if (!rootPane.getStyleClass().contains("light-theme")) {
                rootPane.getStyleClass().add("light-theme");
            }
        } else {
            btnThemeToggle.setSelected(false);
            btnThemeToggle.setText("🌙 Mode Sombre");
            rootPane.getStyleClass().remove("light-theme");
        }

        // Lier les colonnes
        colRepas.setCellValueFactory(new PropertyValueFactory<>("mealType"));
        colCalories.setCellValueFactory(new PropertyValueFactory<>("totalCalories"));
        colProteines.setCellValueFactory(new PropertyValueFactory<>("totalProtein"));
        colGlucides.setCellValueFactory(new PropertyValueFactory<>("totalCarbs"));
        colLipides.setCellValueFactory(new PropertyValueFactory<>("totalFats"));

        // Configuration des filtres et du tri
        setupFiltersAndSorting();

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

    private void setupFiltersAndSorting() {
        // Initialiser les choix du filtre
        comboMealFilter.setItems(FXCollections.observableArrayList(
                "Tous les repas", "BREAKFAST", "LUNCH", "DINNER", "SNACK"
        ));
        comboMealFilter.setValue("Tous les repas");

        // Créer une liste filtrée
        FilteredList<FoodLog> filteredData = new FilteredList<>(foodLogsList, b -> true);

        // Action quand on change le type de repas
        comboMealFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(foodLog -> {
                if (newValue == null || newValue.equals("Tous les repas")) {
                    return true; // Affiche tout
                }
                return foodLog.getMealType().equalsIgnoreCase(newValue); // Filtre par type
            });
        });

        // Créer une liste triée
        SortedList<FoodLog> sortedData = new SortedList<>(filteredData);

        // --- RETOUR DE L'ANCIEN TRI (Clic sur les colonnes) ---
        // On attache le tri de la liste directement au tableau
        sortedData.comparatorProperty().bind(tableHistorique.comparatorProperty());

        // Appliquer les données au tableau
        tableHistorique.setItems(sortedData);
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
    

    



    

    
    

    


    @Override
    protected javafx.scene.Parent getRoot() { return rootPane; }

}