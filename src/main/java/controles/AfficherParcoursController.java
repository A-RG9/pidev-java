package controles;

import entities.parcours_de_sante;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import services.ParcoursDeSanteServices;
import services.ParcoursRecommendationService;
import org.controlsfx.control.RangeSlider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AfficherParcoursController {

    @FXML private FlowPane cardsContainer;
    @FXML private Label countLabel;
    @FXML private Button btnAdd;
    @FXML private TextField searchName;
    @FXML private TextField searchLocation;
    @FXML private RangeSlider distanceRange;
    @FXML private RangeSlider publicationsRange;
    @FXML private Label lblMinDistance;
    @FXML private Label lblMaxDistance;
    @FXML private Label lblMinPubs;
    @FXML private Label lblMaxPubs;
    @FXML private Button btnSearch;
    @FXML private Button btnReset;
    @FXML private ComboBox<String> sortCombo;
    @FXML private Button btnSortAsc;
    @FXML private Button btnSortDesc;

    @FXML private VBox chatWindow;
    @FXML private Button btnToggleChat;
    @FXML private Button btnCloseChat;
    @FXML private VBox chatMessagesContainer;
    @FXML private ScrollPane chatScrollPane;
    @FXML private FlowPane quickChoicesPane;
    @FXML private TextField chatInput;
    @FXML private Button btnSendMessage;

    private final ParcoursDeSanteServices service = new ParcoursDeSanteServices();
    private boolean isAscending = false;

    private ParcoursRecommendationService aiService;

    private double userLat = 37.2744;
    private double userLon = 9.8739;

    private enum ChatState { START, WAITING_WEATHER, WAITING_TRAIL }
    private ChatState currentChatState = ChatState.START;

    private List<ParcoursRecommendationService.RecommendationResult> currentTop3 = null;

    @FXML
    public void initialize() {

        fetchUserRealLocation();

        if (sortCombo != null) {
            sortCombo.setItems(FXCollections.observableArrayList("Creation date", "Name", "Distance", "Publications"));
            sortCombo.setValue("Creation date");
            sortCombo.setOnAction(e -> applyFilters());
        }
        if (btnSortAsc != null && btnSortDesc != null) {
            btnSortAsc.setOnAction(e -> setSortDirection(true));
            btnSortDesc.setOnAction(e -> setSortDirection(false));
            setSortDirection(isAscending);
        }

        loadData();

        if (btnAdd != null) {
            btnAdd.setOnAction(event -> {
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/AjouterParcoursDeSante.fxml"));
                    btnAdd.getScene().setRoot(root);
                } catch (IOException e) { e.printStackTrace(); }
            });
        }
        if (btnSearch != null) btnSearch.setOnAction(event -> applyFilters());
        if (btnReset != null) {
            btnReset.setOnAction(event -> {
                searchName.clear(); searchLocation.clear();
                if (distanceRange != null) { distanceRange.setLowValue(0.0); distanceRange.setHighValue(20.0); }
                if (publicationsRange != null) { publicationsRange.setLowValue(0.0); publicationsRange.setHighValue(200.0); }
                if (sortCombo != null) sortCombo.setValue("Date de création");
                setSortDirection(false); loadData();
            });
        }
        if (distanceRange != null) {
            distanceRange.lowValueProperty().addListener((obs, o, n) -> { if (lblMinDistance != null) lblMinDistance.setText(String.format("%.1f km", n.doubleValue())); applyFilters(); });
            distanceRange.highValueProperty().addListener((obs, o, n) -> { if (lblMaxDistance != null) lblMaxDistance.setText(String.format("%.1f km", n.doubleValue())); applyFilters(); });
        }
        if (publicationsRange != null) {
            publicationsRange.lowValueProperty().addListener((obs, o, n) -> { if (lblMinPubs != null) lblMinPubs.setText(String.valueOf(n.intValue())); applyFilters(); });
            publicationsRange.highValueProperty().addListener((obs, o, n) -> { if (lblMaxPubs != null) lblMaxPubs.setText(String.valueOf(n.intValue())); applyFilters(); });
        }

        setupAIAndChat();
    }


    private void fetchUserRealLocation() {
        new Thread(() -> {
            try {
                URL url = new URL("http://ip-api.com/json/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                String json = response.toString();


                Matcher latMatcher = Pattern.compile("\"lat\"\\s*:\\s*([\\-0-9.]+)").matcher(json);
                if (latMatcher.find()) {
                    userLat = Double.parseDouble(latMatcher.group(1));
                }


                Matcher lonMatcher = Pattern.compile("\"lon\"\\s*:\\s*([\\-0-9.]+)").matcher(json);
                if (lonMatcher.find()) {
                    userLon = Double.parseDouble(lonMatcher.group(1));
                }

                System.out.println("📍 Real location dynamically fetched: Lat " + userLat + ", Lon " + userLon);

            } catch (Exception e) {
                System.out.println("⚠️ Could not fetch live location. Falling back to default coordinates.");
            }
        }).start();
    }

    private void setSortDirection(boolean ascending) {
        this.isAscending = ascending;
        String activeStyle = "-fx-background-color: #00a693; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;";
        String inactiveStyle = "-fx-background-color: white; -fx-border-color: #dfe6e9; -fx-border-radius: 5; -fx-cursor: hand; -fx-text-fill: #2d3436;";
        if (btnSortAsc != null && btnSortDesc != null) {
            btnSortAsc.setStyle(ascending ? activeStyle : inactiveStyle);
            btnSortDesc.setStyle(ascending ? inactiveStyle : activeStyle);
        }
        applyFilters();
    }

    public void loadData() { applyFilters(); }

    private void applyFilters() {
        try {
            String nameQuery = searchName.getText() != null ? searchName.getText().trim().toLowerCase() : "";
            String locationQuery = searchLocation.getText() != null ? searchLocation.getText().trim().toLowerCase() : "";
            double minDistance = distanceRange != null ? distanceRange.getLowValue() : 0.0;
            double maxDistance = distanceRange != null ? distanceRange.getHighValue() : 20.0;
            int minPubs = publicationsRange != null ? (int) publicationsRange.getLowValue() : 0;
            int maxPubs = publicationsRange != null ? (int) publicationsRange.getHighValue() : 200;

            List<parcours_de_sante> allData = service.afficher();

            List<parcours_de_sante> filteredList = allData.stream()
                    .filter(p -> {
                        String nom = p.getNom_parcours() != null ? p.getNom_parcours().toLowerCase() : "";
                        String loc = p.getLocalisation_parcours() != null ? p.getLocalisation_parcours().toLowerCase() : "";
                        return nom.contains(nameQuery) && loc.contains(locationQuery) &&
                                p.getDistance_parcours() >= minDistance && p.getDistance_parcours() <= maxDistance &&
                                ((p.getPublications() != null ? p.getPublications().size() : 0) >= minPubs &&
                                        (p.getPublications() != null ? p.getPublications().size() : 0) <= maxPubs);
                    }).collect(Collectors.toList());

            Comparator<parcours_de_sante> comparator = (p1, p2) -> 0;
            String sortCriteria = sortCombo != null ? sortCombo.getValue() : "Date de création";
            if (sortCriteria != null) {
                switch (sortCriteria) {
                    case "Nom": comparator = Comparator.comparing(p -> p.getNom_parcours() != null ? p.getNom_parcours().toLowerCase() : ""); break;
                    case "Distance": comparator = Comparator.comparingDouble(parcours_de_sante::getDistance_parcours); break;
                    case "Publications": comparator = Comparator.comparingInt(p -> p.getPublications() != null ? p.getPublications().size() : 0); break;
                    default: comparator = Comparator.comparing(p -> p.getDate_creation() != null ? p.getDate_creation() : ""); break;
                }
            }
            if (!isAscending) comparator = comparator.reversed();
            filteredList.sort(comparator);
            updateUI(filteredList);

        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updateUI(List<parcours_de_sante> list) {
        if (countLabel != null) countLabel.setText(list.size() + " Health Trail available");
        cardsContainer.getChildren().clear();
        for (parcours_de_sante p : list) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ParcoursCard.fxml"));
                Parent card = loader.load();
                ParcoursCardController controller = loader.getController();
                controller.setData(p, this::loadData);
                cardsContainer.getChildren().add(card);
            } catch (IOException e) { System.err.println("Error loading card: " + e.getMessage()); }
        }
    }

    private void setupAIAndChat() {
        ParcoursRecommendationService.WeatherServiceInterface weatherFetcher = (lat, lon, locName) -> fetchLiveWeather(lat, lon);
        aiService = new ParcoursRecommendationService(weatherFetcher);

        if (btnToggleChat != null) {
            btnToggleChat.setOnAction(e -> {
                chatWindow.setVisible(!chatWindow.isVisible());
                if (chatWindow.isVisible() && chatMessagesContainer.getChildren().isEmpty()) {
                    showStartMenu("Hello! I can help you find a trail. How would you like to search?");
                }
            });
        }
        if (btnCloseChat != null) btnCloseChat.setOnAction(e -> chatWindow.setVisible(false));
        if (btnSendMessage != null && chatInput != null) {
            btnSendMessage.setOnAction(e -> processUserMessage());
            chatInput.setOnAction(e -> processUserMessage());
        }
    }

    private void showStartMenu(String message) {
        addChatMessage("AI", message);
        currentChatState = ChatState.START;
        setQuickChoices("🌤️By Weather", "📍Nearest 3");
    }

    private void setQuickChoices(String... choices) {
        quickChoicesPane.getChildren().clear();
        for (String choice : choices) {
            Button btn = new Button(choice);
            btn.setStyle("-fx-background-color: #f1f2f6; -fx-text-fill: #00a693; -fx-background-radius: 15; -fx-cursor: hand; -fx-font-size: 12; -fx-font-weight: bold;");
            btn.setOnAction(ev -> {
                String textToSend = choice.contains(" ") ? choice.substring(choice.indexOf(" ") + 1) : choice;
                chatInput.setText(textToSend);
                processUserMessage();
            });
            quickChoicesPane.getChildren().add(btn);
        }
    }

    private void processUserMessage() {
        String msg = chatInput.getText().trim();
        if (msg.isEmpty()) return;

        addChatMessage("User", msg);
        chatInput.clear();
        String lowerMsg = msg.toLowerCase();

        if (currentChatState == ChatState.START) {
            if (lowerMsg.contains("weather")) {
                currentChatState = ChatState.WAITING_WEATHER;
                addChatMessage("AI", "What kind of weather do you prefer today?");
                setQuickChoices("☀️ Clear", "☁️ Cloudy", "🌧️ Rainy", "❄️ Snowy", "💨 Windy");
            } else if (lowerMsg.contains("nearest") || lowerMsg.contains("3")) {
                handleNearest3Request();
            } else {
                addChatMessage("AI", "Please click one of the options below to get started.");
            }
        }
        else if (currentChatState == ChatState.WAITING_WEATHER) {
            handleWeatherRecommendation(lowerMsg);
        }
        else if (currentChatState == ChatState.WAITING_TRAIL) {
            handleTrailSelection(lowerMsg);
        }
    }

    private void handleNearest3Request() {
        addChatMessage("AI", "Finding the closest trails to your real location...");
        quickChoicesPane.getChildren().clear();

        new Thread(() -> {
            try {
                List<parcours_de_sante> allParcours = service.afficher();
                currentTop3 = aiService.getNearestTrails(allParcours, userLat, userLon, 3);

                Platform.runLater(() -> {
                    chatMessagesContainer.getChildren().remove(chatMessagesContainer.getChildren().size() - 1);

                    if (currentTop3.isEmpty()) {
                        showStartMenu("I couldn't find any trails in the database.");
                        return;
                    }

                    StringBuilder sb = new StringBuilder("Here are the 3 closest trails:\n");
                    String[] btnNames = new String[currentTop3.size()];

                    for (int i = 0; i < currentTop3.size(); i++) {
                        ParcoursRecommendationService.RecommendationResult r = currentTop3.get(i);
                        sb.append(i + 1).append(". *").append(r.parcours.getNom_parcours()).append("* (").append(String.format("%.1f", r.distanceKm)).append(" km)\n");
                        btnNames[i] = "🎯 " + r.parcours.getNom_parcours();
                    }
                    sb.append("\nWhich one would you like to check the weather for?");

                    addChatMessage("AI", sb.toString());
                    currentChatState = ChatState.WAITING_TRAIL;
                    setQuickChoices(btnNames);
                });
            } catch (SQLException e) {
                Platform.runLater(() -> showStartMenu("Oops! Database error. Let's start over."));
            }
        }).start();
    }

    private void handleTrailSelection(String lowerMsg) {
        ParcoursRecommendationService.RecommendationResult selected = null;
        for (ParcoursRecommendationService.RecommendationResult r : currentTop3) {
            if (lowerMsg.contains(r.parcours.getNom_parcours().toLowerCase())) {
                selected = r;
                break;
            }
        }

        if (selected == null) {
            addChatMessage("AI", "I didn't recognize that trail name. Please click one of the buttons.");
            return;
        }

        final parcours_de_sante targetTrail = selected.parcours;
        addChatMessage("AI", "Checking weather for " + targetTrail.getNom_parcours() + "...");
        quickChoicesPane.getChildren().clear();

        new Thread(() -> {
            ParcoursRecommendationService.WeatherInfo w = aiService.getWeatherForTrail(targetTrail);
            Platform.runLater(() -> {
                chatMessagesContainer.getChildren().remove(chatMessagesContainer.getChildren().size() - 1);
                String weatherIcon = "☁️";
                if (w.condition.contains("clear")) weatherIcon = "☀️";
                if (w.condition.contains("rain")) weatherIcon = "🌧️";
                if (w.condition.contains("snow")) weatherIcon = "❄️";

                addChatMessage("AI", weatherIcon + " Currently at *" + targetTrail.getNom_parcours() + "*: " + w.condition + " with " + w.windSpeed + " km/h wind.");
                showStartMenu("Is there anything else I can help with?");
            });
        }).start();
    }

    private void handleWeatherRecommendation(String lowerMsg) {
        String preference = "any";
        if (lowerMsg.contains("clear") || lowerMsg.contains("sun")) preference = "clear";
        else if (lowerMsg.contains("cloud") || lowerMsg.contains("fog")) preference = "cloudy";
        else if (lowerMsg.contains("rain") || lowerMsg.contains("drizzle")) preference = "rain";
        else if (lowerMsg.contains("snow")) preference = "snow";
        else if (lowerMsg.contains("wind")) preference = "windy";

        addChatMessage("AI", "Thinking...");
        quickChoicesPane.getChildren().clear();
        final String finalPref = preference;

        new Thread(() -> {
            try {
                List<parcours_de_sante> allParcours = service.afficher();
                ParcoursRecommendationService.RecommendationResult result = aiService.recommendNearestByWeather(allParcours, userLat, userLon, finalPref);

                Platform.runLater(() -> {
                    chatMessagesContainer.getChildren().remove(chatMessagesContainer.getChildren().size() - 1);
                    if (result == null) {
                        addChatMessage("AI", "I'm sorry, I couldn't find any trails that match your criteria right now.");
                    } else {
                        String response = "Based on your preference, I recommend **" + result.parcours.getNom_parcours() + "**!\n"
                                + "📍 Distance: " + String.format("%.1f", result.distanceKm) + " km away.\n"
                                + "☁️ Weather there: " + result.weather.condition + " (" + result.weather.windSpeed + " km/h).";
                        addChatMessage("AI", response);
                    }
                    showStartMenu("Is there anything else I can help with?");
                });
            } catch (SQLException e) {
                Platform.runLater(() -> showStartMenu("Oops! Database error. Let's start over."));
            }
        }).start();
    }

    private void addChatMessage(String sender, String text) {
        VBox bubble = new VBox();
        bubble.setMaxWidth(250);
        bubble.setPadding(new Insets(10));

        Label textLabel = new Label(text);
        textLabel.setWrapText(true);
        textLabel.setStyle("-fx-font-size: 13;");

        if (sender.equals("User")) {
            bubble.setStyle("-fx-background-color: #e0f2f1; -fx-background-radius: 15 15 0 15;");
            bubble.setAlignment(Pos.CENTER_RIGHT);
            HBox wrapper = new HBox(bubble);
            wrapper.setAlignment(Pos.CENTER_RIGHT);
            bubble.getChildren().add(textLabel);
            chatMessagesContainer.getChildren().add(wrapper);
        } else {
            bubble.setStyle("-fx-background-color: #f1f2f6; -fx-background-radius: 15 15 15 0;");
            bubble.setAlignment(Pos.CENTER_LEFT);
            HBox wrapper = new HBox(bubble);
            wrapper.setAlignment(Pos.CENTER_LEFT);
            bubble.getChildren().add(textLabel);
            chatMessagesContainer.getChildren().add(wrapper);
        }
        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }

    private ParcoursRecommendationService.WeatherInfo fetchLiveWeather(double lat, double lon) {
        try {
            String urlString = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon + "&current_weather=true";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000); conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "JavaFX-TrailApp");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();

            String json = response.toString();
            double windspeed = 0.0; int weathercode = 0;

            Matcher wsMatcher = Pattern.compile("\"windspeed\"\\s*:\\s*([0-9.]+)").matcher(json);
            if (wsMatcher.find()) windspeed = Double.parseDouble(wsMatcher.group(1));

            Matcher wcMatcher = Pattern.compile("\"weathercode\"\\s*:\\s*([0-9]+)").matcher(json);
            if (wcMatcher.find()) weathercode = Integer.parseInt(wcMatcher.group(1));

            String condition = "clear";
            if (weathercode >= 1 && weathercode <= 3) condition = "cloudy";
            else if (weathercode == 45 || weathercode == 48) condition = "foggy";
            else if (weathercode >= 51 && weathercode <= 67) condition = "rainy";
            else if (weathercode >= 71 && weathercode <= 77) condition = "snowy";
            else if (weathercode >= 95) condition = "thunderstorm";

            return new ParcoursRecommendationService.WeatherInfo(condition, windspeed);
        } catch (Exception e) {
            return new ParcoursRecommendationService.WeatherInfo("unknown", 0.0);
        }
    }
}