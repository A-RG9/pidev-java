package com.wellora.javafx.controller;

import com.sothawo.mapjfx.Coordinate;
import com.sothawo.mapjfx.MapType;
import com.sothawo.mapjfx.MapView;
import com.sothawo.mapjfx.Marker;
import com.wellora.controllers.BaseController;
import com.wellora.controllers.HealthShellController;
import com.wellora.model.parcours_de_sante;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import com.wellcare.javafx.util.AppConfig;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DetailsParcoursController extends BaseController {

    @FXML private ScrollPane mainScrollPane;

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
    @FXML private Label statTemp;
    @FXML private Label statWeatherDesc;
    @FXML private Label statWind;
    @FXML private Label summaryLocation;
    @FXML private Label summaryDate;
    @FXML private Label summaryCoords;
    @FXML private Label mapLocationLabel;

    @FXML private MapView mapView;
    @FXML private Button btnReturn;
    @FXML private Button btnViewPublications;
    @FXML private Label userDistanceLabel;

    private parcours_de_sante currentParcours;
    private Marker mapMarker;

    @FXML
    void initialize() {
        btnReturn.setOnAction(event -> returnToDisplay());


        if (btnViewPublications != null) {
            btnViewPublications.setOnAction(e -> goToPublications());
        }

        heroImageView.setPreserveRatio(true);

        javafx.beans.value.ChangeListener<Number> coverListener = (obs, oldVal, newVal) -> {
            if (heroImageView.getImage() != null) {
                double imgW = heroImageView.getImage().getWidth();
                double imgH = heroImageView.getImage().getHeight();
                double boxW = heroContainer.getWidth();
                double boxH = heroContainer.getHeight();

                if (imgW > 0 && imgH > 0 && boxW > 0 && boxH > 0) {
                    double scale = Math.max(boxW / imgW, boxH / imgH);
                    heroImageView.setFitWidth(imgW * scale);
                    heroImageView.setFitHeight(imgH * scale);
                }
            }
        };

        heroContainer.widthProperty().addListener(coverListener);
        heroContainer.heightProperty().addListener(coverListener);
        heroImageView.imageProperty().addListener((obs, oldImg, newImg) -> coverListener.changed(null, null, null));

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

        Platform.runLater(() -> {
            if (mainScrollPane != null) {
                mainScrollPane.setVvalue(0.0);
            }
        });

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

        userDistanceLabel.setText("Calcul...");
        new Thread(() -> {
            double[] myLiveLocation = fetchLiveLocationFromIP();
            double distance = calculateDistance(myLiveLocation[0], myLiveLocation[1], p.getLatitude_parcours(), p.getLongitude_parcours());
            Platform.runLater(() -> {
                userDistanceLabel.setText(String.format("%.2f km", distance));
            });
        }).start();

        new Thread(() -> {
            fetchLiveWeather(p.getLatitude_parcours(), p.getLongitude_parcours());
        }).start();

        try {
            LocalDate creationDate = LocalDate.parse(p.getDate_creation());
            long daysBetween = ChronoUnit.DAYS.between(creationDate, LocalDate.now());
            String timeAgo = daysBetween == 0 ? "Aujourd'hui" : " " + daysBetween + " days ago";
            heroDate.setText("📅 " + timeAgo);
            summaryDate.setText(timeAgo);
        } catch (Exception e) {
            heroDate.setText("📅 " + p.getDate_creation());
            summaryDate.setText(p.getDate_creation());
        }

        try {
            String path = p.getImage_parcours();
            if (path != null && !path.isEmpty() && !path.equals("default.png")) {
                File file;
                String normalizedPath = path.startsWith("/") ? path : "/" + path;
                if (normalizedPath.startsWith("/uploads")) {
                    String sharedDir = AppConfig.getSharedUploadDir();
                    if (sharedDir != null && !sharedDir.isEmpty()) {
                        String relativeToUploads = normalizedPath.replace("/uploads/", "");
                        file = new File(sharedDir, relativeToUploads);
                    } else {
                        file = new File(path);
                    }
                } else {
                    file = new File(path);
                }

                if (file.exists()) {
                    // Disable caching to ensure updates are visible
                    heroImageView.setImage(new Image(file.toURI().toString(), 0, 0, true, true, false));
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur image (Details): " + e.getMessage());
        }

        if (mapView.getInitialized()) {
            setMapLocation();
        }
    }

    private void fetchLiveWeather(double lat, double lon) {
        try {
            URL url = new URL("https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon + "&current_weather=true");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            String jsonResponse = response.toString();

            int weatherBlockIndex = jsonResponse.indexOf("\"current_weather\"");
            if (weatherBlockIndex == -1) throw new Exception("No current_weather data found.");

            String weatherBlock = jsonResponse.substring(weatherBlockIndex);

            String tempStr = extractJsonValue(weatherBlock, "\"temperature\":");
            String windStr = extractJsonValue(weatherBlock, "\"windspeed\":");
            String codeStr = extractJsonValue(weatherBlock, "\"weathercode\":");

            int weatherCode = (int) Double.parseDouble(codeStr);
            String weatherDescription = getWeatherDescription(weatherCode);

            Platform.runLater(() -> {
                statTemp.setText(tempStr + " °C");
                statWind.setText(windStr + " km/h");
                statWeatherDesc.setText(weatherDescription);
            });

        } catch (Exception e) {
            System.err.println("Failed to fetch weather: " + e.getMessage());
            Platform.runLater(() -> {
                statTemp.setText("N/A");
                statWind.setText("N/A");
                statWeatherDesc.setText("Offline");
            });
        }
    }

    private String extractJsonValue(String json, String key) {
        int startIndex = json.indexOf(key);
        if (startIndex == -1) return "0";
        startIndex += key.length();
        int endIndex = json.indexOf(",", startIndex);
        if (endIndex == -1) endIndex = json.indexOf("}", startIndex);
        return json.substring(startIndex, endIndex).trim();
    }

    private String getWeatherDescription(int code) {
        if (code == 0) return "Clear sky ☀️";
        if (code >= 1 && code <= 3) return "Partly cloudy ⛅";
        if (code >= 45 && code <= 48) return "Foggy 🌫️";
        if (code >= 51 && code <= 55) return "Drizzle 🌧️";
        if (code >= 61 && code <= 65) return "Rain 🌧️";
        if (code >= 71 && code <= 75) return "Snow ❄️";
        if (code >= 95) return "Thunderstorm 🌩️";
        return "Unknown 🌡️";
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
        if (mainController != null) {
            mainController.loadView("/fxml/AfficherParcours.fxml");
        }
    }

    @Override
    protected Parent getRoot() {
        return null; // Not used in DetailsParcours context - theme is applied via HealthShell
    }

    private void goToPublications() {
        if (currentParcours == null) {
            System.err.println("No parcours selected!");
            return;
        }

        if (mainController != null) {
            mapView.close();
            mainController.loadViewWithData("/fxml/AfficherPublications.fxml", currentParcours);
        }
    }
}