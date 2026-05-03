package com.wellora.javafx.controller;

import com.wellora.model.parcours_de_sante;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.wellora.dao.ParcoursDeSanteDAO;
import com.wellora.controllers.HealthShellController;
import com.wellora.javafx.controller.ModifierParcoursDeSanteController;
import com.wellora.javafx.controller.AfficherPublicationsController;
import com.wellora.javafx.controller.DetailsParcoursController;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class ParcoursCardController {
    @FXML private Label nomLabel;
    @FXML private Label locationLabel;
    @FXML private Label distanceLabel;
    @FXML private Label dateLabel;
    @FXML private Label pubCountLabel;
    @FXML private ImageView imageView;
    @FXML private Button btnDelete;
    @FXML private Button btnEdit;
    @FXML private Button btnView;
    @FXML private Button btnInfo;

    private final ParcoursDeSanteDAO ps = new ParcoursDeSanteDAO();

    public void setData(parcours_de_sante p, Runnable refreshCallback) {
        if (p == null) return;

        nomLabel.setText(p.getNom_parcours());
        locationLabel.setText(p.getLocalisation_parcours());
        distanceLabel.setText(p.getDistance_parcours() + " km");

        if (dateLabel != null) {
            String formattedDate = formatTimeAgo(p.getDate_creation());
            dateLabel.setText(formattedDate);
        }

        if (pubCountLabel != null) {
            int count = (p.getPublications() != null) ? p.getPublications().size() : 0;
            pubCountLabel.setText(count + " Publications");
        }

        String path = p.getImage_parcours();
        if (path != null && !path.trim().isEmpty()) {
            try {
                File file = new File(path);
                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString(), true));
                }
            } catch (Exception e) {

            }
        }

        if (btnDelete != null) {
            btnDelete.setOnAction(event -> {
                try {
                    ps.supprimer(p.getId());
                    refreshCallback.run();
                } catch (SQLException e) {
                    System.err.println("Impossible de supprimer : Une publication est probablement liée à ce parcours.");
                }
            });
        }

        if (btnEdit != null) {
            btnEdit.setOnAction(event -> {
                loadViewWithHealthShell("/fxml/ModifierParcoursDeSante.fxml", p, "ModifierParcoursDeSanteController");
            });
        }

        if (btnView != null) {
            btnView.setOnAction(event -> {
                loadViewWithHealthShell("/fxml/AfficherPublications.fxml", p, "AfficherPublicationsController");
            });
        }
        if (btnInfo != null) {
            btnInfo.setOnAction(event -> {
                loadViewWithHealthShell("/fxml/DetailsParcours.fxml", p, "DetailsParcoursController");
            });
        }
    }

    private void loadViewWithHealthShell(String fxmlPath, parcours_de_sante p, String controllerType) {
        try {
            FXMLLoader shellLoader = new FXMLLoader(getClass().getResource("/com/wellora/views/HealthShell.fxml"));
            Parent shellRoot = shellLoader.load();
            HealthShellController shellCtrl = shellLoader.getController();

            // Load content into shell
            shellCtrl.setContentWithProxy(fxmlPath);

            // After content is loaded, initialize the controller with data
            // We need to get the content controller from the shell
            // Since setContentWithProxy loads asynchronously, we need a different approach
            // Let's load the content first, then set it in the shell
            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent contentRoot = contentLoader.load();

            // Initialize the controller with data based on type
            switch (controllerType) {
                case "ModifierParcoursDeSanteController":
                    ModifierParcoursDeSanteController editCtrl = contentLoader.getController();
                    editCtrl.initData(p);
                    break;
                case "AfficherPublicationsController":
                    AfficherPublicationsController viewCtrl = contentLoader.getController();
                    viewCtrl.initData(p);
                    break;
                case "DetailsParcoursController":
                    DetailsParcoursController detailsCtrl = contentLoader.getController();
                    detailsCtrl.initData(p);
                    break;
            }

            // Set the content in the shell
            shellCtrl.setContent(contentRoot);

            String cssPath = getClass().getResource("/com/wellora/css/style.css").toExternalForm();
            if (!shellRoot.getStylesheets().contains(cssPath)) {
                shellRoot.getStylesheets().add(cssPath);
            }

            Stage stage = (Stage) btnInfo.getScene().getWindow();
            stage.getScene().setRoot(shellRoot);

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String formatTimeAgo(String dateString) {
        if (dateString == null || dateString.isEmpty()) return "Date inconnue";

        try {
            LocalDate creationDate = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate today = LocalDate.now();

            Period period = Period.between(creationDate, today);

            if (period.getYears() > 0) {
                return "" + period.getYears() + " year ago";
            } else if (period.getMonths() > 0) {
                return "" + period.getMonths() + " month ago";
            } else if (period.getDays() > 0) {
                return "" + period.getDays() + " days ago";
            } else {
                return "today";
            }
        } catch (Exception e) {
            System.err.println("Error formatting date '"+dateString+"': " + e.getMessage());
            return dateString;
        }
    }
}