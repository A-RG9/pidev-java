package controles;

import com.sothawo.mapjfx.Coordinate;
import com.sothawo.mapjfx.MapType;
import com.sothawo.mapjfx.MapView;
import com.sothawo.mapjfx.Marker;
import entities.parcours_de_sante;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DetailsParcoursController {


    @FXML private StackPane heroContainer;
    @FXML private ImageView heroImageView;
    @FXML private Rectangle heroOverlay;
    @FXML private Label heroTitle;
    @FXML private Label heroLocation;
    @FXML private Label heroDistance;
    @FXML private Label heroPubs;
    @FXML private Label heroDate;


    @FXML private Label statDistance;
    @FXML private Label statPubs;
    @FXML private Label summaryLocation;
    @FXML private Label summaryDate;
    @FXML private Label summaryCoords;
    @FXML private Label mapLocationLabel;


    @FXML private MapView mapView;
    @FXML private Button btnReturn;
    @FXML private Label userDistanceLabel;

    private parcours_de_sante currentParcours;
    private Marker mapMarker;

    @FXML
    void initialize() {
        btnReturn.setOnAction(event -> returnToDisplay());


        heroImageView.fitWidthProperty().bind(heroContainer.widthProperty());
        heroOverlay.widthProperty().bind(heroContainer.widthProperty());

        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.widthProperty().bind(heroContainer.widthProperty());
        clip.heightProperty().bind(heroContainer.heightProperty());
        clip.setArcWidth(32);
        clip.setArcHeight(32);
        heroContainer.setClip(clip);

        initializeMap();
    }

    private void initializeMap() {
        mapView.initialize();
        mapView.initializedProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                mapView.setMapType(MapType.OSM);
                mapView.setZoom(14);
                if (currentParcours != null) {
                    setMapLocation();
                }
            }
        });
    }

    public void initData(parcours_de_sante p) {
        if (p == null) return;
        this.currentParcours = p;

        heroTitle.setText(p.getNom_parcours().toUpperCase());
        heroLocation.setText("📍 " + p.getLocalisation_parcours());
        heroDistance.setText("📏 " + p.getDistance_parcours() + " km");
        statDistance.setText(String.valueOf(p.getDistance_parcours()));
        summaryLocation.setText(p.getLocalisation_parcours());
        mapLocationLabel.setText(p.getLocalisation_parcours());
        summaryCoords.setText(String.format("%.6f, %.6f", p.getLatitude_parcours(), p.getLongitude_parcours()));


        int pubCount = (p.getPublications() != null) ? p.getPublications().size() : 0;
        heroPubs.setText("📄 " + pubCount + " publications");
        statPubs.setText(String.valueOf(pubCount));

        userDistanceLabel.setText("Calcul en cours...");
        new Thread(() -> {
            double[] myLiveLocation = fetchLiveLocationFromIP();
            double distance = calculateDistance(myLiveLocation[0], myLiveLocation[1], p.getLatitude_parcours(), p.getLongitude_parcours());
            Platform.runLater(() -> {
                userDistanceLabel.setText(String.format("%.2f km", distance));
            });
        }).start();


        try {
            LocalDate creationDate = LocalDate.parse(p.getDate_creation());
            long daysBetween = ChronoUnit.DAYS.between(creationDate, LocalDate.now());
            String timeAgo = daysBetween == 0 ? "Aujourd'hui" : "il y a " + daysBetween + " jours";
            heroDate.setText("📅 " + timeAgo);
            summaryDate.setText(timeAgo);
        } catch (Exception e) {
            heroDate.setText("📅 " + p.getDate_creation());
            summaryDate.setText(p.getDate_creation());
        }

        try {
            if (p.getImage_parcours() != null && !p.getImage_parcours().isEmpty() && !p.getImage_parcours().equals("default.png")) {
                File file = new File(p.getImage_parcours());
                if (file.exists()) {
                    heroImageView.setImage(new Image(file.toURI().toString()));
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur image: " + e.getMessage());
        }

        if (mapView.getInitialized()) {
            setMapLocation();
        }
    }

    private double[] fetchLiveLocationFromIP() {
        try {
            URL url = new URL("http://ip-api.com/csv/?fields=lat,lon");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = reader.readLine();

            if (line != null && line.contains(",")) {
                String[] parts = line.split(",");
                return new double[]{Double.parseDouble(parts[0]), Double.parseDouble(parts[1])};
            }
        } catch (Exception e) {
            System.err.println("Could not fetch live location.");
        }
        return new double[]{36.8065, 10.1815};
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void setMapLocation() {
        Coordinate coord = new Coordinate(currentParcours.getLatitude_parcours(), currentParcours.getLongitude_parcours());
        mapView.setCenter(coord);

        try {
            URL modernMarkerUrl = new URL("https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png");
            mapMarker = new Marker(modernMarkerUrl, -12, -41).setPosition(coord).setVisible(true);
            mapView.addMarker(mapMarker);
        } catch (MalformedURLException e) {
            mapMarker = Marker.createProvided(Marker.Provided.RED).setPosition(coord).setVisible(true);
            mapView.addMarker(mapMarker);
        }
    }

    private void returnToDisplay() {
        try {
            mapView.close();
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherParcours.fxml"));
            btnReturn.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("[Navigation Error] : " + e.getMessage());
        }
    }
}