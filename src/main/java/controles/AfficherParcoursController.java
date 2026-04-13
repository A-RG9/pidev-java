package controles;

import entities.parcours_de_sante;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import services.ParcoursDeSanteServices;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AfficherParcoursController {

    @FXML private FlowPane cardsContainer;
    @FXML private Label countLabel;
    @FXML private Button btnAdd;

    @FXML private TextField searchName;
    @FXML private TextField searchLocation;
    @FXML private Slider distanceSlider;
    @FXML private Slider pubSlider;
    @FXML private Button btnSearch;
    @FXML private Button btnReset;
    @FXML private Label distanceValueLabel;
    @FXML private Label pubValueLabel;

    @FXML private ComboBox<String> sortCombo;
    @FXML private Button btnSortAsc;
    @FXML private Button btnSortDesc;

    private final ParcoursDeSanteServices service = new ParcoursDeSanteServices();


    private boolean isAscending = false;

    @FXML
    public void initialize() {

        if (sortCombo != null) {
            sortCombo.setItems(FXCollections.observableArrayList(
                    "Date de création", "Nom", "Distance", "Publications"
            ));
            sortCombo.setValue("Date de création");

            sortCombo.setOnAction(e -> applyFilters());
        }


        if (btnSortAsc != null && btnSortDesc != null) {
            btnSortAsc.setOnAction(e -> setSortDirection(true));
            btnSortDesc.setOnAction(e -> setSortDirection(false));
            // Apply initial styling
            setSortDirection(isAscending);
        }

        loadData();

        if (btnAdd != null) {
            btnAdd.setOnAction(event -> {
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/AjouterParcoursDeSante.fxml"));
                    btnAdd.getScene().setRoot(root);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        if (btnSearch != null) btnSearch.setOnAction(event -> applyFilters());

        if (btnReset != null) {
            btnReset.setOnAction(event -> {
                searchName.clear();
                searchLocation.clear();
                distanceSlider.setValue(20.0);
                pubSlider.setValue(200.0);
                if (distanceValueLabel != null) distanceValueLabel.setText("20.0 km");
                if (pubValueLabel != null) pubValueLabel.setText("200");


                if (sortCombo != null) sortCombo.setValue("Date de création");
                setSortDirection(false);

                loadData();
            });
        }

        if (distanceSlider != null) {
            distanceSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (distanceValueLabel != null) {
                    distanceValueLabel.setText(String.format("%.1f km", newVal.doubleValue()));
                }
                applyFilters();
            });
        }

        if (pubSlider != null) {
            pubSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (pubValueLabel != null) {
                    pubValueLabel.setText(String.valueOf(newVal.intValue()));
                }
                applyFilters();
            });
        }
    }


    private void setSortDirection(boolean ascending) {
        this.isAscending = ascending;

        String activeStyle = "-fx-background-color: #00a693; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;";
        String inactiveStyle = "-fx-background-color: white; -fx-border-color: #dfe6e9; -fx-border-radius: 5; -fx-cursor: hand; -fx-text-fill: #2d3436;";

        if (btnSortAsc != null && btnSortDesc != null) {
            if (ascending) {
                btnSortAsc.setStyle(activeStyle);
                btnSortDesc.setStyle(inactiveStyle);
            } else {
                btnSortAsc.setStyle(inactiveStyle);
                btnSortDesc.setStyle(activeStyle);
            }
        }


        applyFilters();
    }

    public void loadData() {
        try {

            applyFilters();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyFilters() {
        try {
            String nameQuery = searchName.getText() != null ? searchName.getText().trim().toLowerCase() : "";
            String locationQuery = searchLocation.getText() != null ? searchLocation.getText().trim().toLowerCase() : "";

            double maxDistance = distanceSlider != null ? distanceSlider.getValue() : 20.0;
            double maxPubs = pubSlider != null ? pubSlider.getValue() : 200.0;

            List<parcours_de_sante> allData = service.afficher();


            List<parcours_de_sante> filteredList = allData.stream()
                    .filter(p -> {
                        String nom = p.getNom_parcours() != null ? p.getNom_parcours().toLowerCase() : "";
                        String loc = p.getLocalisation_parcours() != null ? p.getLocalisation_parcours().toLowerCase() : "";

                        boolean matchesName = nom.contains(nameQuery);
                        boolean matchesLoc = loc.contains(locationQuery);
                        boolean matchesDistance = p.getDistance_parcours() <= maxDistance;

                        int pubCount = (p.getPublications() != null) ? p.getPublications().size() : 0;
                        boolean matchesPubs = pubCount <= maxPubs;

                        return matchesName && matchesLoc && matchesDistance && matchesPubs;
                    })
                    .collect(Collectors.toList());


            Comparator<parcours_de_sante> comparator = (p1, p2) -> 0;
            String sortCriteria = sortCombo != null ? sortCombo.getValue() : "Date de création";

            if (sortCriteria != null) {
                switch (sortCriteria) {
                    case "Nom":
                        comparator = Comparator.comparing(p -> p.getNom_parcours() != null ? p.getNom_parcours().toLowerCase() : "");
                        break;
                    case "Distance":
                        comparator = Comparator.comparingDouble(parcours_de_sante::getDistance_parcours);
                        break;
                    case "Publications":
                        comparator = Comparator.comparingInt(p -> p.getPublications() != null ? p.getPublications().size() : 0);
                        break;
                    case "Date de création":
                    default:
                        comparator = Comparator.comparing(p -> p.getDate_creation() != null ? p.getDate_creation() : "");
                        break;
                }
            }


            if (!isAscending) {
                comparator = comparator.reversed();
            }

            filteredList.sort(comparator);

            updateUI(filteredList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateUI(List<parcours_de_sante> list) {
        if (countLabel != null) {
            countLabel.setText(list.size() + " parcours available");
        }

        cardsContainer.getChildren().clear();

        for (parcours_de_sante p : list) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ParcoursCard.fxml"));
                Parent card = loader.load();

                ParcoursCardController controller = loader.getController();
                controller.setData(p, this::loadData);

                cardsContainer.getChildren().add(card);
            } catch (IOException e) {
                System.err.println("Error loading card: " + e.getMessage());
            }
        }
    }
}