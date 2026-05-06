package com.wellora.javafx.controller;

import com.wellora.javafx.controller.MainController;
import com.wellora.model.parcours_de_sante;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.wellora.dao.ParcoursDeSanteDAO;
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
    private MainController mainController;

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

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
                if (mainController != null) {
                    mainController.loadViewWithData("/fxml/ModifierParcoursDeSante.fxml", p);
                }
            });
        }

        if (btnView != null) {
            btnView.setOnAction(event -> {
                if (mainController != null) {
                    mainController.loadViewWithData("/fxml/AfficherPublications.fxml", p);
                }
            });
        }
        if (btnInfo != null) {
            btnInfo.setOnAction(event -> {
                if (mainController != null) {
                    mainController.loadViewWithData("/fxml/DetailsParcours.fxml", p);
                }
            });
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