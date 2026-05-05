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
import java.util.Optional;

import com.wellora.utils.ThemeManager;

public class JournalController extends BaseController {

    @FXML private BorderPane rootPane;
    @FXML private ToggleButton btnThemeToggle;
    @FXML private DatePicker datePicker;
    @FXML private Label lblCalories, lblProteines, lblGlucides, lblLipides;

    // Filtres
    @FXML private ComboBox<String> comboMealFilter;

    @FXML private TableView<FoodLog> tableHistorique;
    @FXML private TableColumn<FoodLog, String> colRepas;
    @FXML private TableColumn<FoodLog, Integer> colCalories;
    @FXML private TableColumn<FoodLog, Double> colProteines, colGlucides, colLipides;

    private final FoodLogDAO dao = new FoodLogDAO();
    private final String currentUserUuid = "c513e200-d605-4f61-9efc-d539f0e80914";
    private ObservableList<FoodLog> foodLogsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // --- TEST DE CONNEXION BDD ---
        try {
            java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:mysql://localhost:3306/wellora", "root", "");
            System.out.println("✅ DATABASE CONNECTED SUCCESSFULLY (Journal)!");
        } catch (Exception e) {
            showCustomAlert(Alert.AlertType.ERROR, "CRITICAL DB ERROR", "Why it failed:\n" + e.getMessage());
            e.printStackTrace();
        }
        // ------------------------------

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

        // Charger les données initiales safely
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
        try {
            double[] totals = dao.getDailyTotals(currentUserUuid, date);
            lblCalories.setText(String.format("%.0f kcal", totals[0]));
            lblProteines.setText(String.format("%.0fg", totals[1]));
            lblGlucides.setText(String.format("%.0fg", totals[2]));
            lblLipides.setText(String.format("%.0fg", totals[3]));

            List<FoodLog> logs = dao.getFoodLogsByDate(currentUserUuid, date);
            foodLogsList.setAll(logs);

            // Gestion de l'affichage si la BDD est vide pour cette date
            if (logs.isEmpty()) {
                tableHistorique.setPlaceholder(new Label("Aucun historique de repas pour cette date."));
            }

        } catch (Exception e) {
            System.err.println("Error fetching history: " + e.getMessage());
            tableHistorique.setPlaceholder(new Label("Erreur de connexion à la base de données."));
            showCustomAlert(Alert.AlertType.ERROR, "Database Connection Error",
                    "Impossible de récupérer l'historique.\nDétails: " + e.getMessage());
        }
    }

    // --- ALERTE CUSTOM POUR LES ERREURS ---
    private Optional<ButtonType> showCustomAlert(Alert.AlertType type, String title, String message, ButtonType... buttonTypes) {
        Alert alert;
        if (buttonTypes.length > 0) {
            alert = new Alert(type, message, buttonTypes);
        } else {
            alert = new Alert(type, message);
        }

        alert.setTitle(title);
        alert.setHeaderText(null);

        DialogPane dialogPane = alert.getDialogPane();
        try {
            dialogPane.getStylesheets().add(getClass().getResource("/com/wellora/css/style.css").toExternalForm());
            dialogPane.getStyleClass().add("card");

            if (ThemeManager.isDarkMode) {
                dialogPane.getStyleClass().add("light-theme");
                dialogPane.setStyle("-fx-background-color: #FFFFFF; -fx-font-family: 'Segoe UI', sans-serif;");
            } else {
                dialogPane.setStyle("-fx-background-color: #1F2937; -fx-font-family: 'Segoe UI', sans-serif;");
                javafx.scene.Node content = dialogPane.lookup(".content.label");
                if (content != null) content.setStyle("-fx-text-fill: white;");
            }
        } catch (Exception e) {
            System.out.println("CSS non trouvé pour l'alerte.");
        }

        for (ButtonType btnType : dialogPane.getButtonTypes()) {
            Button btn = (Button) dialogPane.lookupButton(btnType);
            if (btn != null) {
                if (btnType == ButtonType.OK || btnType == ButtonType.YES) {
                    btn.getStyleClass().add("primary-button");
                } else if (btnType == ButtonType.NO || btnType == ButtonType.CANCEL) {
                    btn.getStyleClass().add("secondary-button");
                }
            }
        }

        return alert.showAndWait();
    }

    @Override
    protected javafx.scene.Parent getRoot() { return rootPane; }
}