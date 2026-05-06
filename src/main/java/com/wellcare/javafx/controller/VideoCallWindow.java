package com.wellcare.javafx.controller;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class VideoCallWindow {

    private Stage stage;
    private WebView webView;
    private WebEngine webEngine;
    private boolean isMuted = false;
    private boolean isVideoOff = false;
    private ProgressIndicator loadingIndicator;

    public void show(String meetingUrl, String title, String participantName) {
        stage = new Stage();
        stage.setTitle(title + " - Wellora Visioconférence");
        stage.setMinWidth(1000);
        stage.setMinHeight(750);
        stage.initStyle(StageStyle.DECORATED);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(50, 50);

        // WebView
        webView = new WebView();
        webEngine = webView.getEngine();
        webView.setVisible(false);

        // Gestion du chargement
        webEngine.getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
            if (newState == javafx.concurrent.Worker.State.RUNNING) {
                showLoading(true);
            } else if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                showLoading(false);
                webView.setVisible(true);
            } else if (newState == javafx.concurrent.Worker.State.FAILED) {
                showLoading(false);
                showErrorPage();
            }
        });

        webEngine.load(meetingUrl);

        // Animation d'entrée
        FadeTransition ft = new FadeTransition(Duration.millis(300), root);
        ft.setFromValue(0);
        ft.setToValue(1);

        // StackPane pour overlay loading
        StackPane centerPane = new StackPane();
        centerPane.getChildren().addAll(webView, loadingIndicator);

        // Barre de contrôle
        VBox controls = createControlBar();

        // Header
        HBox header = createHeader(participantName);

        root.setTop(header);
        root.setCenter(centerPane);
        root.setBottom(controls);

        Scene scene = new Scene(root);
        stage.setScene(scene);

        stage.setOnCloseRequest(e -> close());

        stage.show();
        ft.play();
    }

    private HBox createHeader(String participantName) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #0f172a; -fx-padding: 10 15;");
        header.setPrefHeight(50);

        Label titleLabel = new Label("🎥 " + participantName);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        Label statusLabel = new Label("🔴 En appel");
        statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 8; -fx-background-color: rgba(239,68,68,0.2); -fx-background-radius: 10;");

        header.getChildren().addAll(titleLabel, statusLabel);

        return header;
    }

    private VBox createControlBar() {
        VBox controls = new VBox();
        controls.setStyle("-fx-background-color: #0f172a; -fx-padding: 12;");
        controls.setAlignment(Pos.CENTER);

        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);

        // Bouton Micro
        Button micButton = new Button("🎤");
        micButton.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-min-width: 45; -fx-min-height: 45; -fx-background-radius: 25; -fx-cursor: hand;");
        micButton.setOnAction(e -> toggleMicro(micButton));
        Tooltip.install(micButton, new Tooltip("Couper/activer le micro"));

        // Bouton Caméra
        Button camButton = new Button("📷");
        camButton.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-min-width: 45; -fx-min-height: 45; -fx-background-radius: 25; -fx-cursor: hand;");
        camButton.setOnAction(e -> toggleCamera(camButton));
        Tooltip.install(camButton, new Tooltip("Couper/activer la caméra"));

        // Bouton Partager écran
        Button shareButton = new Button("🖥️");
        shareButton.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-min-width: 45; -fx-min-height: 45; -fx-background-radius: 25; -fx-cursor: hand;");
        shareButton.setOnAction(e -> shareScreen());
        Tooltip.install(shareButton, new Tooltip("Partager l'écran"));

        // Bouton Quitter
        Button endButton = new Button("❌");
        endButton.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-min-width: 45; -fx-min-height: 45; -fx-background-radius: 25; -fx-cursor: hand; -fx-font-weight: bold;");
        endButton.setOnAction(e -> close());
        Tooltip.install(endButton, new Tooltip("Quitter l'appel"));

        buttons.getChildren().addAll(micButton, camButton, shareButton, endButton);

        // Qualité appel
        Label qualityLabel = new Label("🔒 Chiffré");
        qualityLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 10px; -fx-padding: 5;");

        controls.getChildren().addAll(buttons, qualityLabel);

        return controls;
    }

    private void toggleMicro(Button button) {
        isMuted = !isMuted;
        button.setStyle(isMuted ?
                "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-min-width: 45; -fx-min-height: 45; -fx-background-radius: 25; -fx-cursor: hand;" :
                "-fx-background-color: #334155; -fx-text-fill: white; -fx-min-width: 45; -fx-min-height: 45; -fx-background-radius: 25; -fx-cursor: hand;");
        button.setText(isMuted ? "🔇" : "🎤");

        runScript("if(window.JitsiMeetJS && APP.conference) {" +
                "  APP.conference.getConference().isAudioMuted() ? " +
                "  APP.conference.getConference().unmuteAudio() : " +
                "  APP.conference.getConference().muteAudio();" +
                "}");
    }

    private void toggleCamera(Button button) {
        isVideoOff = !isVideoOff;
        button.setStyle(isVideoOff ?
                "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-min-width: 45; -fx-min-height: 45; -fx-background-radius: 25; -fx-cursor: hand;" :
                "-fx-background-color: #334155; -fx-text-fill: white; -fx-min-width: 45; -fx-min-height: 45; -fx-background-radius: 25; -fx-cursor: hand;");
        button.setText(isVideoOff ? "📷❌" : "📷");

        runScript("if(window.JitsiMeetJS && APP.conference) {" +
                "  APP.conference.getConference().isVideoMuted() ? " +
                "  APP.conference.getConference().unmuteVideo() : " +
                "  APP.conference.getConference().muteVideo();" +
                "}");
    }

    private void shareScreen() {
        runScript("if(window.JitsiMeetJS && APP.conference) {" +
                "  APP.conference.getConference().toggleScreenSharing();" +
                "}");
    }

    private void runScript(String script) {
        if (webEngine != null) {
            Platform.runLater(() -> webEngine.executeScript(script));
        }
    }

    private void showLoading(boolean show) {
        Platform.runLater(() -> {
            loadingIndicator.setVisible(show);
            if (!show) {
                loadingIndicator.setManaged(false);
            }
        });
    }

    private void showErrorPage() {
        String errorHtml = """
            <html>
            <head>
                <style>
                    body {
                        margin: 0;
                        background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        height: 100vh;
                        font-family: Arial, sans-serif;
                    }
                    .container {
                        text-align: center;
                        color: white;
                        background: rgba(0,0,0,0.5);
                        padding: 40px;
                        border-radius: 20px;
                    }
                    h1 { font-size: 48px; margin: 0 0 20px; }
                    .link {
                        display: inline-block;
                        margin-top: 20px;
                        padding: 12px 24px;
                        background: #14b8a6;
                        color: white;
                        text-decoration: none;
                        border-radius: 25px;
                        font-weight: bold;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🎥</h1>
                    <h2>Ouvrir dans le navigateur</h2>
                    <p>Cliquez sur le lien ci-dessous pour rejoindre l'appel :</p>
                    <a href=%s class="link">Rejoindre l'appel</a>
                </div>
            </body>
            </html>
        """;
        webEngine.loadContent(String.format(errorHtml, "\"#\""));
    }

    public void close() {
        if (webView != null) {
            webEngine.load(null);
        }
        if (stage != null) {
            stage.close();
        }
    }
}