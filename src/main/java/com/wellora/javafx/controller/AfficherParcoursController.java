package com.wellora.javafx.controller;

import com.wellora.model.parcours_de_sante;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import com.wellora.dao.ParcoursDeSanteDAO;
import com.wellora.services.ParcoursRecommendationService;
import com.wellora.controllers.HealthShellController;
import javafx.stage.Stage;
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

    private final ParcoursDeSanteDAO service = new ParcoursDeSanteDAO();
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
                    FXMLLoader shellLoader = new FXMLLoader(getClass().getResource("/com/wellora/views/HealthShell.fxml"));
                    Parent shellRoot = shellLoader.load();
                    HealthShellController shellCtrl = shellLoader.getController();

                    shellCtrl.setContentWithProxy("/fxml/AjouterParcoursDeSante.fxml");

                    String cssPath = getClass().getResource("/com/wellora/css/style.css").toExternalForm();
                    if (!shellRoot.getStylesheets().contains(cssPath)) {
                        shellRoot.getStylesheets().add(cssPath);
                    }

                    Stage stage = (Stage) btnAdd.getScene().getWindow();
                    stage.getScene().setRoot(shellRoot);
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ParcoursCard.fxml"));
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

        // Chat starts hidden
        chatWindow.setVisible(false);

        // Toggle chat visibility with floating button
        if (btnToggleChat != null) {
            btnToggleChat.setOnAction(e -> {
                boolean newVisibility = !chatWindow.isVisible();
                chatWindow.setVisible(newVisibility);
                if (newVisibility && chatMessagesContainer.getChildren().isEmpty()) {
                    showStartMenu("Hello! I can help you find a trail. How would you like to search?");
                }
            });
        }

        // Close chat
        if (btnCloseChat != null) btnCloseChat.setOnAction(e -> chatWindow.setVisible(false));

        // Send message
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

   /**
    * Adds a message bubble to the chat conversation with modern styling.
    *
    * @param sender  "AI" or "User"
    * @param message The message text (can contain markdown-style **bold**)
    */
   private void addChatMessage(String sender, String message) {
       // Main message container
       HBox messageBox = new HBox();
       messageBox.setMaxWidth(400.0);
       messageBox.setPadding(new Insets(4, 8, 4, 8));

       // AI messages align left, User messages align right
       if (sender.equals("AI")) {
           messageBox.setAlignment(Pos.CENTER_LEFT);
           HBox.setHgrow(messageBox, Priority.NEVER);
       } else {
           messageBox.setAlignment(Pos.CENTER_RIGHT);
           HBox.setHgrow(messageBox, Priority.ALWAYS);
       }

       // Create message bubble with modern styling
       VBox bubble = new VBox();
       bubble.setMaxWidth(320.0);
       bubble.setPadding(new Insets(14, 18, 14, 18));
       bubble.setSpacing(6.0);

       // Modern styles with subtle effects
       if (sender.equals("AI")) {
           bubble.setStyle(
               "-fx-background-color: white;" +
               "-fx-background-radius: 18 18 18 4;" +
               "-fx-border-color: #e3fcf9;" +
               "-fx-border-width: 1;" +
               "-fx-border-radius: 18;" +
               "-fx-effect: dropshadow(gaussian, rgba(0, 166, 147, 0.12), 6, 0, 0, 2);"
           );
       } else {
           bubble.setStyle(
               "-fx-background-color: #00a693;" +
               "-fx-background-radius: 18 18 4 18;" +
               "-fx-effect: dropshadow(gaussian, rgba(0, 166, 147, 0.25), 8, 0, 0, 3);"
           );
       }

       // Create header region with avatar and name
       HBox headerBox = new HBox();
       headerBox.setSpacing(8.0);
       headerBox.setAlignment(Pos.CENTER_LEFT);

       // Avatar for AI
       if (sender.equals("AI")) {
           Label avatar = new Label("🤖");
           avatar.setStyle("-fx-font-size: 18; -fx-text-fill: #00a693;");
           headerBox.getChildren().add(avatar);
       }

       // Sender name label
       Label nameLabel = new Label(sender.equals("AI") ? "Assistant" : "You");
       nameLabel.setStyle(
           "-fx-font-size: 12; " +
           "-fx-font-weight: bold; " +
           "-fx-text-fill: " + (sender.equals("AI") ? "#00a693" : "white") + ";"
       );
       headerBox.getChildren().add(nameLabel);

       // Add header to bubble
       bubble.getChildren().add(headerBox);

       // Create message content with proper formatting
       if (message.contains("**")) {
           // Parse markdown-style bold text
           TextFlow textFlow = new TextFlow();
           textFlow.setMaxWidth(300.0);
           textFlow.setLineSpacing(2.0);

           String[] parts = message.split("\\*\\*");
           for (int i = 0; i < parts.length; i++) {
               Text textPart = new Text(parts[i]);
               textPart.setStyle("-fx-font-size: 13;");

               if (i % 2 == 1) { // Bold section
                   textPart.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");
               }

               if (sender.equals("AI")) {
                   textPart.setFill(javafx.scene.paint.Color.web("#2d3436"));
               } else {
                   textPart.setFill(javafx.scene.paint.Color.WHITE);
               }

               textFlow.getChildren().add(textPart);
           }
           bubble.getChildren().add(textFlow);
       } else {
           Label msgLabel = new Label(message);
           msgLabel.setWrapText(true);
           msgLabel.setMaxWidth(300.0);
           msgLabel.setStyle(
               "-fx-font-size: 13; " +
               "-fx-text-fill: " + (sender.equals("AI") ? "#2d3436" : "white") + ";" +
               "-fx-line-spacing: 2px;"
           );
           bubble.getChildren().add(msgLabel);
       }

       // Add timestamp (optional, subtle)
       Label timeLabel = new Label(java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
       timeLabel.setStyle(
           "-fx-font-size: 10; " +
           "-fx-text-fill: " + (sender.equals("AI") ? "#b2bec3" : "rgba(255,255,255,0.7)") + ";" +
           "-fx-padding: 4 0 0 0;"
       );
       bubble.getChildren().add(timeLabel);

       // Add bubble to message box
       messageBox.getChildren().add(bubble);

       // Add margin for message box
       VBox.setMargin(messageBox, new Insets(2, 0, 2, 0));

       // Add to container with fade-in animation
       chatMessagesContainer.getChildren().add(messageBox);

       // Fade-in animation for new message
       messageBox.setOpacity(0.0);
       javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), messageBox);
       fadeIn.setFromValue(0.0);
       fadeIn.setToValue(1.0);
       fadeIn.play();

       // Scroll to bottom smoothly
       Platform.runLater(() -> {
           javafx.animation.Interpolator interpolator = javafx.animation.Interpolator.EASE_BOTH;
           javafx.animation.KeyValue kv = new javafx.animation.KeyValue(chatScrollPane.vvalueProperty(), 1.0, interpolator);
           javafx.animation.KeyFrame kf = new javafx.animation.KeyFrame(javafx.util.Duration.millis(200), kv);
           javafx.animation.Timeline timeline = new javafx.animation.Timeline(kf);
           timeline.play();
       });
   }
}
