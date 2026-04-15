package controles;

import entities.parcours_de_sante;
import entities.publication_parcours;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import services.PublicationServices;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
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
                        return date1.compareTo(date2); // Ascending order
                    } else {
                        return date2.compareTo(date1); // Descending order (Newest first)
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
                VBox card = new VBox(15);
                card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 2);");
                card.setMaxWidth(800);

                HBox header = new HBox(10);
                header.setAlignment(Pos.CENTER_LEFT);

                StackPane avatar = new StackPane();
                Circle circle = new Circle(20, javafx.scene.paint.Color.web("#009688"));
                Label initials = new Label("UT");
                initials.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");
                avatar.getChildren().addAll(circle, initials);

                VBox userInfo = new VBox(2);
                Label userName = new Label("Utilisateur");
                userName.setStyle("-fx-font-weight: bold; -fx-font-size: 15; -fx-text-fill: #2d3436;");
                Label timeAgo = new Label(formatTimeAgo(pub.getDate_publication()));
                timeAgo.setStyle("-fx-text-fill: #b2bec3; -fx-font-size: 12;");
                userInfo.getChildren().addAll(userName, timeAgo);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button btnEditCard = new Button("📝");
                btnEditCard.setStyle("-fx-background-color: transparent; -fx-text-fill: #3498db; -fx-cursor: hand; -fx-font-size: 14;");
                Button btnDeleteCard = new Button("🗑");
                btnDeleteCard.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-cursor: hand; -fx-font-size: 14;");

                btnDeleteCard.setOnAction(e -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation de suppression");
                    alert.setHeaderText(null);
                    alert.setContentText("Êtes-vous sûr de vouloir supprimer cette publication ?");
                    Optional<ButtonType> result = alert.showAndWait();

                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            pubService.supprimer(pub.getId());
                            loadPublications();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                btnEditCard.setOnAction(e -> goToModifierPublication(pub));

                header.getChildren().addAll(avatar, userInfo, spacer, btnEditCard, btnDeleteCard);

                HBox badgesRow = new HBox(15);
                badgesRow.setAlignment(Pos.CENTER_LEFT);

                Label ambLbl = new Label("⭐ " + pub.getAmbiance() + "/5 ambiance");
                ambLbl.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold; -fx-font-size: 13;");

                Label secLbl = new Label("🛡 " + pub.getSecurite() + "/5 safety");
                secLbl.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-font-size: 13;");

                String expValue = pub.getExperience() != null ? pub.getExperience() : "Unknown";
                String expColor = "#7f8c8d";
                String expIcon = "•";

                if (expValue.equalsIgnoreCase("Excellent")) {
                    expColor = "#3498db";
                    expIcon = "🌟";
                } else if (expValue.equalsIgnoreCase("Good") || expValue.equalsIgnoreCase("Bien")) {
                    expColor = "#2ecc71";
                    expIcon = "🙂";
                } else if (expValue.equalsIgnoreCase("Average") || expValue.equalsIgnoreCase("Moyen")) {
                    expColor = "#f39c12";
                    expIcon = "😐";
                } else if (expValue.equalsIgnoreCase("Poor") || expValue.equalsIgnoreCase("Bad") || expValue.equalsIgnoreCase("Mauvais")) {
                    expColor = "#e74c3c";
                    expIcon = "☹";
                }

                Label expLbl = new Label(expIcon + " " + expValue);
                expLbl.setStyle("-fx-text-fill: " + expColor + "; -fx-font-weight: bold; -fx-font-size: 13;");

                Label typeBadge = new Label(pub.getType_publication());
                typeBadge.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #7f8c8d; -fx-padding: 3 8 3 8; -fx-background-radius: 10; -fx-font-size: 12;");

                badgesRow.getChildren().addAll(ambLbl, secLbl, expLbl, typeBadge);

                Label textContent = new Label(pub.getText_publication());
                textContent.setStyle("-fx-text-fill: #2d3436; -fx-font-size: 14;");
                textContent.setWrapText(true);

                ImageView imageView = new ImageView();
                if (pub.getImage_publication() != null && !pub.getImage_publication().isEmpty()) {
                    try {
                        File file = new File(pub.getImage_publication());
                        if (file.exists()) {
                            Image image = new Image(file.toURI().toString());
                            imageView.setImage(image);
                            imageView.setFitWidth(750);
                            imageView.setPreserveRatio(true);
                        }
                    } catch (Exception e) {
                        System.out.println("Image non trouvée : " + pub.getImage_publication());
                    }
                }

                HBox footer = new HBox(5);
                footer.setAlignment(Pos.CENTER_LEFT);
                footer.setPadding(new Insets(10, 0, 0, 0));
                footer.setStyle("-fx-border-color: #ecf0f1 transparent transparent transparent; -fx-border-width: 1 0 0 0;");

                Label commentsLbl = new Label("💬 0 comments");
                commentsLbl.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 13; -fx-cursor: hand;");
                footer.getChildren().add(commentsLbl);

                card.getChildren().addAll(header, badgesRow, textContent);
                if (imageView.getImage() != null) {
                    card.getChildren().add(imageView);
                }
                card.getChildren().add(footer);

                publicationsContainer.getChildren().add(card);
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

    private String formatTimeAgo(String dateString) {
        if (dateString == null || dateString.isEmpty()) return "Date inconnue";
        try {
            LocalDate creationDate = LocalDate.parse(dateString.split(" ")[0], DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate today = LocalDate.now();
            Period period = Period.between(creationDate, today);

            if (period.getYears() > 0) return "il y a " + period.getYears() + " an(s)";
            if (period.getMonths() > 0) return "il y a " + period.getMonths() + " mois";
            if (period.getDays() > 0) return "il y a " + period.getDays() + " jour(s)";
            return "aujourd'hui";
        } catch (Exception e) {
            return dateString;
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

    private void goToModifierPublication(publication_parcours pubToEdit) {
        if (currentParcours == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierPublication.fxml"));
            Parent root = loader.load();

            ModifierPublicationController controller = loader.getController();
            controller.initData(currentParcours, pubToEdit);

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