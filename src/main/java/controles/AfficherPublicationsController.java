package controles;

import entities.parcours_de_sante;
import entities.publication_parcours;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import services.PublicationServices;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AfficherPublicationsController {

    @FXML private Label trailNameLabel;
    @FXML private VBox publicationsContainer;
    @FXML private Button btnBackToTrails;
    @FXML private Button btnWritePublication;
    @FXML private Button btnAddPublication;

    @FXML private ComboBox<String> comboExperience;
    @FXML private ComboBox<String> comboType;
    @FXML private ComboBox<String> comboSort;
    @FXML private TextField searchHashtag;

    private parcours_de_sante currentParcours;
    private final PublicationServices pubService = new PublicationServices();

    @FXML
    public void initialize() {
        if (btnBackToTrails != null) {
            btnBackToTrails.setOnAction(e -> goBack());
        }

        if (btnWritePublication != null) {
            btnWritePublication.setOnAction(e -> goToAjouterPublication());
        }
        if (btnAddPublication != null) {
            btnAddPublication.setOnAction(e -> goToAjouterPublication());
        }

        if (comboExperience != null) {
            comboExperience.getItems().clear();
            comboExperience.getItems().addAll("All experiences", "Bad", "Good", "Excellent");
            comboExperience.setValue("All experiences");
            comboExperience.setOnAction(e -> loadPublications());
        }

        if (comboType != null) {
            comboType.getItems().clear();
            comboType.getItems().addAll("All types", "Review", "Event");
            comboType.setValue("All types");
            comboType.setOnAction(e -> loadPublications());
        }

        if (comboSort != null) {
            comboSort.getItems().clear();
            comboSort.getItems().addAll("Newest pub", "Oldest pub");
            comboSort.setValue("Newest pub");
            comboSort.setOnAction(e -> loadPublications());
        }
    }

    public void initData(parcours_de_sante p) {
        this.currentParcours = p;
        if (trailNameLabel != null) {
            trailNameLabel.setText(p.getNom_parcours());
        }
        loadPublications();
    }

    private void loadPublications() {
        publicationsContainer.getChildren().clear();
        try {
            List<publication_parcours> list = pubService.afficherParParcours(currentParcours.getId());


            if (comboExperience != null && comboExperience.getValue() != null) {
                String selectedExp = comboExperience.getValue();
                if (!selectedExp.equals("All experiences")) {
                    list = list.stream()
                            .filter(pub -> pub.getExperience() != null && pub.getExperience().equalsIgnoreCase(selectedExp))
                            .collect(Collectors.toList());
                }
            }

            if (comboType != null && comboType.getValue() != null) {
                String selectedType = comboType.getValue();
                if (!selectedType.equals("All types")) {
                    list = list.stream()
                            .filter(pub -> pub.getType_publication() != null && pub.getType_publication().equalsIgnoreCase(selectedType))
                            .collect(Collectors.toList());
                }
            }


            if (comboSort != null && comboSort.getValue() != null) {
                String sortChoice = comboSort.getValue();
                list.sort((pub1, pub2) -> {
                    LocalDate date1 = parseDateSafely(pub1.getDate_publication());
                    LocalDate date2 = parseDateSafely(pub2.getDate_publication());

                    if (sortChoice.equals("Oldest pub")) {
                        return date1.compareTo(date2);
                    } else {
                        return date2.compareTo(date1);
                    }
                });
            }


            if (list.isEmpty()) {
                VBox emptyStateBox = new VBox(15);
                emptyStateBox.setAlignment(Pos.CENTER);
                emptyStateBox.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 50; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);");
                emptyStateBox.setMinHeight(200);

                Label emptyLabel = new Label("No publications found for this filter combination.");
                emptyLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #636e72;");

                Button btnClearFilters = new Button("Clear Filters");
                btnClearFilters.setStyle("-fx-background-color: #009688; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20; -fx-cursor: hand;");
                btnClearFilters.setOnAction(e -> {
                    if (comboExperience != null) comboExperience.setValue("All experiences");
                    if (comboType != null) comboType.setValue("All types");
                    if (comboSort != null) comboSort.setValue("Newest pub");
                    loadPublications();
                });

                emptyStateBox.getChildren().addAll(emptyLabel, btnClearFilters);
                publicationsContainer.getChildren().add(emptyStateBox);
                return;
            }


            for (publication_parcours pub : list) {
                try {

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/PublicationCard.fxml"));
                    Parent cardNode = loader.load();


                    PublicationCardController controller = loader.getController();


                    controller.setData(pub, currentParcours, this::loadPublications);


                    publicationsContainer.getChildren().add(cardNode);

                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.err.println("Failed to load PublicationCard.fxml for publication ID: " + pub.getId());
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private LocalDate parseDateSafely(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return LocalDate.MIN;
        }
        try {
            return LocalDate.parse(dateString.split(" ")[0], DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            return LocalDate.MIN;
        }
    }

    private void goToAjouterPublication() {
        if (currentParcours == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterPublication.fxml"));
            Parent root = loader.load();

            AjouterPublicationController controller = loader.getController();
            controller.initData(currentParcours);

            btnBackToTrails.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherParcours.fxml"));
            btnBackToTrails.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}