package controles;

import entities.parcours_de_sante;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import services.ParcoursDeSanteServices;

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
    @FXML private Button btnView; // New button added here

    private final ParcoursDeSanteServices ps = new ParcoursDeSanteServices();

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
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierParcoursDeSante.fxml"));
                    Parent root = loader.load();

                    ModifierParcoursDeSanteController controller = loader.getController();
                    controller.initData(p);

                    btnEdit.getScene().setRoot(root);
                } catch (IOException e) {
                    System.err.println("Erreur lors du chargement de la page de modification : " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }

        if (btnView != null) {
            btnView.setOnAction(event -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherPublications.fxml"));
                    Parent root = loader.load();

                    AfficherPublicationsController controller = loader.getController();
                    controller.initData(p);

                    btnView.getScene().setRoot(root);
                } catch (IOException e) {
                    System.err.println("Erreur lors de l'ouverture des publications : " + e.getMessage());
                    e.printStackTrace();
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
                return "il y a " + period.getYears() + " an(s)";
            } else if (period.getMonths() > 0) {
                return "il y a " + period.getMonths() + " mois";
            } else if (period.getDays() > 0) {
                return "il y a " + period.getDays() + " jour(s)";
            } else {
                return "aujourd'hui";
            }
        } catch (Exception e) {
            System.err.println("Error formatting date '"+dateString+"': " + e.getMessage());
            return dateString;
        }
    }
}