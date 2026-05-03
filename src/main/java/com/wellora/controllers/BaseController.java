package com.wellora.controllers;

import com.wellora.utils.ThemeManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * BaseController — shared sidebar navigation for all PIDEV-JAVA full-page controllers.
 * Each controller extends this and gets navigation to both Nutrition and Santé views.
 */
public abstract class BaseController {

    @FXML protected ToggleButton btnThemeToggle;

    // ===================== NUTRITION NAVIGATION =====================

    @FXML public void navToDashboard(ActionEvent event)    { switchScene(event, "/com/wellora/views/Dashboard.fxml"); }
    @FXML public void navToJournal(ActionEvent event)      { switchScene(event, "/com/wellora/views/Journal.fxml"); }
    @FXML public void navToObjectifs(ActionEvent event)    { switchScene(event, "/com/wellora/views/Objectif.fxml"); }
    @FXML public void navToPlanificateur(ActionEvent event){ switchScene(event, "/com/wellora/views/Planificateur.fxml"); }
    @FXML public void navToRecettes(ActionEvent event)     { switchScene(event, "/com/wellora/views/Recettes.fxml"); }
    @FXML public void navToAnalyse(ActionEvent event)      { switchScene(event, "/com/wellora/views/Analyse.fxml"); }

    // ===================== SANTÉ NAVIGATION =====================

    @FXML public void navToHome(ActionEvent event)           { switchHealthScene(event, "/fxml/homepage.fxml"); }
    @FXML public void navToHealthDashboard(ActionEvent event){ switchHealthScene(event, "/fxml/dashboard.fxml"); }
    @FXML public void navToHealthJournals(ActionEvent event) { switchHealthScene(event, "/fxml/healthjournal-list.fxml"); }
    @FXML public void navToHealthEntries(ActionEvent event)  { switchHealthScene(event, "/fxml/healthentry-list.fxml"); }
    @FXML public void navToCalendar(ActionEvent event)       { switchHealthScene(event, "/fxml/calendar.fxml"); }
    @FXML public void navToPrediction(ActionEvent event)     { switchHealthScene(event, "/fxml/prediction.fxml"); }

    // ===================== HEALTH TRAIL NAVIGATION =====================

    @FXML public void showAfficherParcours(ActionEvent event)    { switchHealthScene(event, "/fxml/AfficherParcours.fxml"); }
    @FXML public void showAjouterParcours(ActionEvent event)     { switchHealthScene(event, "/fxml/AjouterParcoursDeSante.fxml"); }
    @FXML public void showToutesPublications(ActionEvent event)  { switchHealthScene(event, "/fxml/ToutesPublications.fxml"); }

    // ===================== HEALTH TRAIL QUICK LINKS (from AfficherPublications) =====================

    @FXML public void goBack() {
        try {
            FXMLLoader shellLoader = new FXMLLoader(getClass().getResource("/com/wellora/views/HealthShell.fxml"));
            Parent shellRoot = shellLoader.load();
            HealthShellController shellCtrl = shellLoader.getController();
            shellCtrl.setContentWithProxy("/fxml/AfficherParcours.fxml");
            Stage stage = (Stage) ((Node)btnThemeToggle).getScene().getWindow();
            stage.getScene().setRoot(shellRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML public void goToGlobalFeed() {
        try {
            FXMLLoader shellLoader = new FXMLLoader(getClass().getResource("/com/wellora/views/HealthShell.fxml"));
            Parent shellRoot = shellLoader.load();
            HealthShellController shellCtrl = shellLoader.getController();
            shellCtrl.setContentWithProxy("/fxml/ToutesPublications.fxml");
            Stage stage = (Stage) ((Node)btnThemeToggle).getScene().getWindow();
            stage.getScene().setRoot(shellRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ===================== THEME TOGGLE =====================

    @FXML
    public void toggleTheme(ActionEvent event) {
        boolean isLight = btnThemeToggle != null && btnThemeToggle.isSelected();
        ThemeManager.isDarkMode = !isLight;
        applyThemeToRoot(getRoot(), isLight);
        if (btnThemeToggle != null) {
            btnThemeToggle.setText(isLight ? "☀️ Mode Clair" : "🌙 Mode Sombre");
        }
    }

    /** Override to return the root pane of the view */
    protected abstract Parent getRoot();

    // ===================== HELPERS =====================

    /**
     * Switch to a Nutrition view (uses PIDEV-JAVA style.css, full page swap)
     */
    protected void switchScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            String cssPath = getClass().getResource("/com/wellora/css/style.css").toExternalForm();
            if (!root.getStylesheets().contains(cssPath)) {
                root.getStylesheets().add(cssPath);
            }

            boolean isLight = btnThemeToggle != null && btnThemeToggle.isSelected();
            applyThemeToRoot(root, isLight);

            // Sync theme toggle state on destination
            ToggleButton nextBtn = (ToggleButton) root.lookup("#btnThemeToggle");
            if (nextBtn != null) {
                nextBtn.setSelected(isLight);
                nextBtn.setText(isLight ? "☀️ Mode Clair" : "🌙 Mode Sombre");
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            System.err.println("❌ Cannot load view: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Switch to a Santé/integ view wrapped in PIDEV-JAVA shell.
     * The health view is embedded inside a HealthShellController.
     */
    protected void switchHealthScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader shellLoader = new FXMLLoader(
                getClass().getResource("/com/wellora/views/HealthShell.fxml"));
            Parent shellRoot = shellLoader.load();
            HealthShellController shellCtrl = shellLoader.getController();

            // Load content AND inject proxy for in-shell navigation
            shellCtrl.setContentWithProxy(fxmlPath);

            String cssPath = getClass().getResource("/com/wellora/css/style.css").toExternalForm();
            if (!shellRoot.getStylesheets().contains(cssPath)) {
                shellRoot.getStylesheets().add(cssPath);
            }

            boolean isLight = btnThemeToggle != null && btnThemeToggle.isSelected();
            applyThemeToRoot(shellRoot, isLight);

            ToggleButton nextBtn = (ToggleButton) shellRoot.lookup("#btnThemeToggle");
            if (nextBtn != null) {
                nextBtn.setSelected(isLight);
                nextBtn.setText(isLight ? "☀️ Mode Clair" : "🌙 Mode Sombre");
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(shellRoot);

        } catch (IOException e) {
            System.err.println("❌ Cannot load health view: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void applyThemeToRoot(Parent root, boolean isLight) {
        root.getStyleClass().remove("light-theme");
        if (isLight) {
            root.getStyleClass().add("light-theme");
        }
    }
}
