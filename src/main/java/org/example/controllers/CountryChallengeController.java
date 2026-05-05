package org.example.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.utils.CountryChallengeService;
import org.example.utils.CountryChallengeService.ChallengeResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CountryChallengeController {

    @FXML private ImageView countryFlag;
    @FXML private Label countryName;
    @FXML private Label countryRegion;
    @FXML private Label countryEmoji;
    @FXML private Label countryPopulation;
    @FXML private Label countryRegionValue;
    @FXML private Label challengesCompleted;
    @FXML private Label challengeEmoji;
    @FXML private Label challengeText;
    @FXML private Label challengeDescription;
    @FXML private Label funFactLabel;
    @FXML private Button btnAcceptChallenge;
    @FXML private ListView<String> completedChallengesList;

    private ChallengeResponse currentChallenge;
    private List<String> completedChallenges = new ArrayList<>();
    private int challengeCount = 0;

    @FXML
    public void initialize() {
        // Précharger les pays en arrière-plan
        CountryChallengeService.preloadCountries();

        // Afficher un défi instantané (sans attendre l'API)
        showInstantChallenge();

        // Charger le fun fact
        loadFunFact();
        setupCompletedList();
    }

    private void showInstantChallenge() {
        // Défi instantané (temps de réponse < 1ms)
        countryName.setText("Chargement du défi...");
        challengeText.setText("Préparation de votre défi sportif...");

        new Thread(() -> {
            ChallengeResponse challenge = CountryChallengeService.getRandomCountryChallenge();
            Platform.runLater(() -> {
                currentChallenge = challenge;
                updateUI(challenge);
            });
        }).start();
    }

    @FXML
    private void refreshChallenge() {
        // Animation de chargement rapide
        challengeText.setText("🔄 Nouveau défi en préparation...");

        new Thread(() -> {
            ChallengeResponse challenge = CountryChallengeService.getRandomCountryChallenge();
            Platform.runLater(() -> {
                currentChallenge = challenge;
                updateUI(challenge);
            });
        }).start();
    }

    @FXML
    private void acceptChallenge() {
        if (currentChallenge != null) {
            String completed = String.format("✅ %s - %s (%s)",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    currentChallenge.countryName,
                    currentChallenge.challenge
            );
            completedChallenges.add(0, completed);
            challengeCount++;
            challengesCompleted.setText(String.valueOf(challengeCount));
            completedChallengesList.setItems(FXCollections.observableArrayList(completedChallenges));

            showSuccessMessage("🎉 Félicitations ! Défi de " + currentChallenge.countryName + " accepté !");
            refreshChallenge();
        }
    }

    private void updateUI(ChallengeResponse challenge) {
        countryName.setText(challenge.countryName);
        countryRegion.setText(challenge.region);
        countryRegionValue.setText(challenge.region);
        countryEmoji.setText(challenge.emoji);
        challengeEmoji.setText(challenge.emoji);
        challengeText.setText(challenge.challenge);
        challengeDescription.setText(challenge.description);

        // Population formatée
        if (challenge.population >= 1_000_000_000) {
            countryPopulation.setText(String.format("%.1f milliards", challenge.population / 1_000_000_000.0));
        } else if (challenge.population >= 1_000_000) {
            countryPopulation.setText(String.format("%.1f millions", challenge.population / 1_000_000.0));
        } else {
            countryPopulation.setText(String.valueOf(challenge.population));
        }

        // Charger le drapeau (optionnel, ne bloque pas l'affichage)
        if (challenge.flagUrl != null && !challenge.flagUrl.isEmpty()) {
            loadFlagAsync(challenge.flagUrl);
        }
    }

    private void loadFlagAsync(String flagUrl) {
        new Thread(() -> {
            try {
                Image image = new Image(flagUrl, 80, 50, true, true);
                Platform.runLater(() -> countryFlag.setImage(image));
            } catch (Exception e) {
                // Ignorer - le drapeau n'est pas essentiel
            }
        }).start();
    }

    private void loadFunFact() {
        funFactLabel.setText(CountryChallengeService.getRandomFunFact());
    }

    private void setupCompletedList() {
        completedChallengesList.setStyle("-fx-font-size: 13px; -fx-padding: 10;");
    }

    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bravo !");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}