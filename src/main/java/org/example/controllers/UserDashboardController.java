package org.example.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.example.utils.Database;
import org.example.utils.JitsiService;
import org.example.utils.WebSocketClientService;
import org.json.simple.JSONObject;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class UserDashboardController {

    // ==================== FXML COMPOSANTS ====================
    @FXML private Label patientName, patientEmail;
    @FXML private Label lblUnreadCount, lblGoalsCount, lblPlansCount;
    @FXML private Label lblUnreadCountStat;  // Nouveau
    @FXML private Label lblConnectionStatus, lblCoachStatus;
    @FXML private TextField searchConversation, messageInput;
    @FXML private ListView<ConversationItem> conversationsList;
    @FXML private VBox messagesContainer;
    @FXML private Label selectedCoachName;
    @FXML private ScrollPane messagesScrollPane;
    @FXML private Label lblTypingIndicator;
    @FXML private StackPane contentArea;
    @FXML private Button btnJoinCall, btnStartCall;

    // Visioconférence
    @FXML private VBox videoCallOverlay;
    @FXML private WebView videoWebView;
    @FXML private Label lblCallStatus;
    @FXML private Label lblCallDuration;

    // ==================== VARIABLES ====================
    private String currentPatientUuid;
    private String currentPatientName;
    private String currentCoachUuid;
    private String currentCoachName;
    private int currentConversationId;

    private WebSocketClientService webSocketService;
    private JitsiService jitsiService = new JitsiService();
    private boolean isInCall = false;
    private Timer callDurationTimer;
    private boolean isTyping = false;
    private Timer typingTimer;

    // ==================== INITIALISATION ====================
    @FXML
    public void initialize() {
        loadPatientInfo();
        loadConversations();
        setupSearchListener();
        setupTypingDetection();
        connectWebSocket();
        loadStats();

        if (videoCallOverlay != null) {
            videoCallOverlay.setVisible(false);
        }
    }

    private void setupTypingDetection() {
        if (messageInput != null) {
            messageInput.textProperty().addListener((obs, old, newVal) -> {
                if (!newVal.isEmpty() && !isTyping) {
                    isTyping = true;
                    sendTypingStatus(true);
                    if (typingTimer != null) typingTimer.cancel();
                    typingTimer = new Timer();
                    typingTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> {
                                isTyping = false;
                                sendTypingStatus(false);
                            });
                        }
                    }, 1000);
                }
            });
        }
    }

    // ==================== WEBSOCKET ====================

    private void connectWebSocket() {
        if (currentPatientUuid == null) {
            System.out.println("Patient UUID not loaded yet, will retry...");
            return;
        }
        webSocketService = new WebSocketClientService();
        webSocketService.connect(
                "ws://localhost:8887",
                currentPatientUuid,
                "patient",
                this::onWebSocketMessage,
                this::onWebSocketStatus
        );
    }

    private void onWebSocketMessage(JSONObject message) {
        Platform.runLater(() -> {
            String type = (String) message.get("type");

            switch (type) {
                case "message":
                    String content = (String) message.get("content");
                    String senderId = (String) message.get("senderId");

                    if (senderId != null && senderId.equals(currentCoachUuid)) {
                        addMessageToChat(content, LocalDateTime.now(), false);
                        markMessagesAsRead(currentConversationId);

                        // Notification pour lien vidéo
                        if (content.contains("meet.jit.si")) {
                            showNotification("Nouvel appel vidéo", "Votre coach vous a envoyé un lien pour un appel vidéo !");
                        }
                    }
                    break;

                case "typing":
                    String userId = (String) message.get("userId");
                    boolean typing = (boolean) message.get("isTyping");
                    if (userId != null && userId.equals(currentCoachUuid)) {
                        showTypingIndicator(typing);
                    }
                    break;

                case "status":
                    String statusUserId = (String) message.get("userId");
                    String status = (String) message.get("status");
                    if (statusUserId != null && statusUserId.equals(currentCoachUuid)) {
                        updateCoachStatus(status);
                    }
                    break;
            }
        });
    }

    private void onWebSocketStatus(String status) {
        Platform.runLater(() -> {
            if (lblConnectionStatus != null) {
                lblConnectionStatus.setText(status.contains("Connecté") ? "🟢" : "🔴");
                lblConnectionStatus.setStyle(status.contains("Connecté") ? "-fx-text-fill: #10B981;" : "-fx-text-fill: #EF4444;");
            }
        });
    }

    private void sendTypingStatus(boolean typing) {
        if (webSocketService != null && webSocketService.isConnected() && currentConversationId > 0 && currentCoachUuid != null) {
            webSocketService.sendTyping(String.valueOf(currentConversationId), currentPatientUuid, typing);
        }
    }

    private void showTypingIndicator(boolean typing) {
        Platform.runLater(() -> {
            if (lblTypingIndicator != null) {
                if (typing) {
                    lblTypingIndicator.setText("👨‍🏫 Votre coach est en train d'écrire...");
                } else {
                    lblTypingIndicator.setText("");
                }
            }
        });
    }

    private void updateCoachStatus(String status) {
        Platform.runLater(() -> {
            if (lblCoachStatus != null) {
                if (status.equals("online")) {
                    lblCoachStatus.setText("🟢 En ligne");
                    lblCoachStatus.setStyle("-fx-text-fill: #10B981; -fx-font-size: 11px;");
                    if (btnJoinCall != null) btnJoinCall.setDisable(false);
                    if (btnStartCall != null) btnStartCall.setDisable(false);
                } else {
                    lblCoachStatus.setText("⚫ Hors ligne");
                    lblCoachStatus.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px;");
                    if (btnJoinCall != null) btnJoinCall.setDisable(true);
                    if (btnStartCall != null) btnStartCall.setDisable(true);
                }
            }
        });
    }

    // ==================== CHARGEMENT DES DONNÉES ====================

    private void loadPatientInfo() {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT uuid, first_name, last_name, email FROM users WHERE role = 'ROLE_PATIENT' LIMIT 1";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                currentPatientUuid = rs.getString("uuid");
                currentPatientName = rs.getString("first_name") + " " + rs.getString("last_name");
                if (patientName != null) patientName.setText(currentPatientName);
                if (patientEmail != null) patientEmail.setText(rs.getString("email"));

                loadCoachInfo();
                connectWebSocket();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCoachInfo() {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT u.uuid, u.first_name, u.last_name FROM conversation c " +
                    "JOIN users u ON c.coach_uuid = u.uuid " +
                    "WHERE c.patient_uuid = ? LIMIT 1";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, currentPatientUuid);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                currentCoachUuid = rs.getString("uuid");
                currentCoachName = rs.getString("first_name") + " " + rs.getString("last_name");
                if (selectedCoachName != null) selectedCoachName.setText(currentCoachName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadConversations() {
        if (conversationsList == null) return;
        conversationsList.getItems().clear();

        try (Connection conn = Database.getConnection()) {
            String sql =
                    "SELECT " +
                            "  c.id as conversation_id, " +
                            "  u.uuid as coach_uuid, " +
                            "  u.first_name, " +
                            "  u.last_name, " +
                            "  (SELECT content FROM message WHERE conversation_id = c.id ORDER BY sent_at DESC LIMIT 1) as last_message, " +
                            "  (SELECT COUNT(*) FROM message WHERE conversation_id = c.id AND is_read = 0 AND sender_uuid != ?) as unread_count " +
                            "FROM conversation c " +
                            "JOIN users u ON c.coach_uuid = u.uuid " +
                            "WHERE c.patient_uuid = ?";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, currentPatientUuid);
            pstmt.setString(2, currentPatientUuid);
            ResultSet rs = pstmt.executeQuery();

            int totalUnread = 0;
            while (rs.next()) {
                int unreadCount = rs.getInt("unread_count");
                ConversationItem conv = new ConversationItem(
                        rs.getInt("conversation_id"),
                        rs.getString("coach_uuid"),
                        rs.getString("first_name") + " " + rs.getString("last_name"),
                        rs.getString("last_message"),
                        unreadCount
                );
                conversationsList.getItems().add(conv);
                totalUnread += unreadCount;
            }

            if (lblUnreadCount != null) lblUnreadCount.setText(String.valueOf(totalUnread));
            if (lblUnreadCountStat != null) lblUnreadCountStat.setText(String.valueOf(totalUnread));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        conversationsList.setCellFactory(lv -> new ListCell<ConversationItem>() {
            @Override
            protected void updateItem(ConversationItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox cell = new HBox(10);
                    cell.setAlignment(Pos.CENTER_LEFT);
                    cell.setStyle("-fx-padding: 10;");

                    Label nameLabel = new Label(item.getName());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E293B;");

                    Label messageLabel = new Label(item.getLastMessage() != null ? item.getLastMessage() : "Aucun message");
                    messageLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");

                    VBox textBox = new VBox(3);
                    textBox.getChildren().addAll(nameLabel, messageLabel);
                    HBox.setHgrow(textBox, Priority.ALWAYS);

                    cell.getChildren().addAll(textBox);

                    if (item.getUnreadCount() > 0) {
                        Label badge = new Label(String.valueOf(item.getUnreadCount()));
                        badge.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-padding: 2 6; -fx-background-radius: 10; -fx-font-size: 10px;");
                        cell.getChildren().add(badge);
                    }

                    setGraphic(cell);
                }
            }
        });

        conversationsList.setOnMouseClicked(event -> {
            ConversationItem selected = conversationsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                currentCoachUuid = selected.getCoachUuid();
                currentCoachName = selected.getName();
                currentConversationId = selected.getConversationId();
                if (selectedCoachName != null) selectedCoachName.setText(currentCoachName);
                loadMessages(currentConversationId);
                markMessagesAsRead(currentConversationId);
                if (btnJoinCall != null) btnJoinCall.setDisable(false);
                if (btnStartCall != null) btnStartCall.setDisable(false);
            }
        });
    }

    private void loadMessages(int conversationId) {
        if (messagesContainer == null) return;
        messagesContainer.getChildren().clear();

        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT content, sent_at, sender_uuid FROM message WHERE conversation_id = ? ORDER BY sent_at ASC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, conversationId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String content = rs.getString("content");
                LocalDateTime sentAt = rs.getTimestamp("sent_at").toLocalDateTime();
                String senderUuid = rs.getString("sender_uuid");
                boolean isFromCoach = senderUuid != null && senderUuid.equals(currentCoachUuid);
                addMessageToChat(content, sentAt, isFromCoach);
            }

            if (messagesScrollPane != null) messagesScrollPane.setVvalue(1.0);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void markMessagesAsRead(int conversationId) {
        try (Connection conn = Database.getConnection()) {
            String sql = "UPDATE message SET is_read = 1, read_at = ? WHERE conversation_id = ? AND sender_uuid != ? AND is_read = 0";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(2, conversationId);
            pstmt.setString(3, currentPatientUuid);
            pstmt.executeUpdate();
            loadConversations();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadStats() {
        try (Connection conn = Database.getConnection()) {
            String goalSql = "SELECT COUNT(*) FROM goal WHERE patient_id = ?";
            PreparedStatement goalStmt = conn.prepareStatement(goalSql);
            goalStmt.setString(1, currentPatientUuid);
            ResultSet goalRs = goalStmt.executeQuery();
            if (goalRs.next() && lblGoalsCount != null) {
                lblGoalsCount.setText(String.valueOf(goalRs.getInt(1)));
            }

            String planSql = "SELECT COUNT(*) FROM daily_plan dp JOIN conversation c ON dp.goal_id = c.goal_id WHERE c.patient_uuid = ?";
            PreparedStatement planStmt = conn.prepareStatement(planSql);
            planStmt.setString(1, currentPatientUuid);
            ResultSet planRs = planStmt.executeQuery();
            if (planRs.next() && lblPlansCount != null) {
                lblPlansCount.setText(String.valueOf(planRs.getInt(1)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== ENVOI DE MESSAGES ====================

    @FXML
    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (message.isEmpty() || currentConversationId == 0) return;

        try (Connection conn = Database.getConnection()) {
            String sql = "INSERT INTO message (content, sent_at, is_read, conversation_id, sender_uuid) VALUES (?, ?, 0, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, message);
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(3, currentConversationId);
            pstmt.setString(4, currentPatientUuid);
            pstmt.executeUpdate();

            String updateSql = "UPDATE conversation SET last_message_at = ? WHERE id = ?";
            PreparedStatement pstmt2 = conn.prepareStatement(updateSql);
            pstmt2.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt2.setInt(2, currentConversationId);
            pstmt2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (webSocketService != null && webSocketService.isConnected()) {
            webSocketService.sendMessage(
                    String.valueOf(currentConversationId),
                    message,
                    currentPatientUuid,
                    currentCoachUuid
            );
        }

        addMessageToChat(message, LocalDateTime.now(), false);
        messageInput.clear();
        loadConversations();
    }

    private void addMessageToChat(String message, LocalDateTime time, boolean isFromCoach) {
        if (messagesContainer == null) return;

        HBox messageRow = new HBox();
        messageRow.setAlignment(isFromCoach ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        messageRow.setStyle("-fx-padding: 4;");

        VBox messageBubble = new VBox(4);
        messageBubble.setMaxWidth(350);

        // Vérifier si le message contient un lien Jitsi
        if (message.contains("meet.jit.si") || message.contains("Session vidéo")) {
            messageBubble.setStyle("-fx-background-color: #EFF6FF; -fx-background-radius: 12; -fx-padding: 10 12;");

            final String finalUrl = message.contains("🔗 Session vidéo: ") ?
                    message.replace("🔗 Session vidéo: ", "") : message;

            Hyperlink link = new Hyperlink(message.length() > 50 ? message.substring(0, 47) + "..." : message);
            link.setStyle("-fx-text-fill: #3b82f6; -fx-underline: true; -fx-cursor: hand; -fx-font-size: 12px;");
            link.setOnAction(e -> openInBrowser(finalUrl));

            Label timeLabel = new Label(time.format(DateTimeFormatter.ofPattern("HH:mm")));
            timeLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 9px;");

            messageBubble.getChildren().addAll(link, timeLabel);
        } else {
            String bubbleStyle = isFromCoach
                    ? "-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-padding: 8 12;"
                    : "-fx-background-color: #14b8a6; -fx-background-radius: 12; -fx-padding: 8 12;";
            messageBubble.setStyle(bubbleStyle);

            Label messageLabel = new Label(message);
            messageLabel.setStyle("-fx-text-fill: " + (isFromCoach ? "#1e293b" : "white") + "; -fx-wrap-text: true; -fx-font-size: 13px;");
            messageLabel.setMaxWidth(320);

            Label timeLabel = new Label(time.format(DateTimeFormatter.ofPattern("HH:mm")));
            timeLabel.setStyle("-fx-text-fill: " + (isFromCoach ? "#94a3b8" : "#ccfbf1") + "; -fx-font-size: 9px;");

            messageBubble.getChildren().addAll(messageLabel, timeLabel);
        }

        messageRow.getChildren().add(messageBubble);
        messagesContainer.getChildren().add(messageRow);

        if (messagesScrollPane != null) {
            messagesScrollPane.setVvalue(1.0);
        }
    }

    // ==================== VISIOCONFÉRENCE ====================

    @FXML
    private void startVideoCall() {
        if (currentCoachUuid == null) {
            showAlert("Aucun coach assigné");
            return;
        }

        String roomName = "wellora_" + currentPatientUuid + "_" + System.currentTimeMillis();




        try (Connection conn = Database.getConnection()) {
            String sql = "INSERT INTO message (content, sent_at, is_read, conversation_id, sender_uuid) VALUES (?, ?, 0, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(3, currentConversationId);
            pstmt.setString(4, currentPatientUuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        }



    @FXML
    private void joinVideoCall() {
        if (currentCoachUuid == null) {
            showAlert("Aucun coach assigné");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Rejoindre l'appel");
        dialog.setHeaderText("Entrez le lien de la réunion envoyé par votre coach");
        dialog.setContentText("Lien Jitsi Meet:");

        dialog.showAndWait().ifPresent(url -> {
            if (!url.isEmpty()) {
                openInBrowser(url);
            }
        });
    }

    private void openInBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information");
                alert.setHeaderText(null);
                alert.setContentText("Copiez ce lien dans votre navigateur: " + url);
                alert.showAndWait();
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            showAlert("Erreur: " + e.getMessage());
        }
    }

    private void showNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void closeVideoCall() {
        if (videoCallOverlay != null) videoCallOverlay.setVisible(false);
        isInCall = false;
    }

    @FXML private void toggleMicro() { }
    @FXML private void toggleCamera() { }

    private void setupSearchListener() {
        if (searchConversation != null && conversationsList != null) {
            searchConversation.textProperty().addListener((obs, old, val) -> {
                conversationsList.setItems(conversationsList.getItems().filtered(
                        conv -> conv.getName().toLowerCase().contains(val.toLowerCase())
                ));
            });
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ==================== NAVIGATION ====================
    @FXML private void loadDashboard() { loadPage("/UserDashboard.fxml"); }
    @FXML private void loadGoals() { loadPage("/GoalView.fxml"); }
    @FXML private void loadPlans() { loadPage("/DailyPlanView.fxml"); }
    @FXML private void loadExercises() { loadPage("/ExerciseLibrary.fxml"); }
    @FXML private void loadProgress() { loadPage("/ProgressView.fxml"); }
    @FXML private void loadMessages() { }
    @FXML private void loadVideoCall() { joinVideoCall(); }
    @FXML private void loadAICoach() { loadPage("/AICoachView.fxml"); }
    @FXML private void loadCalendar() { loadPage("/WorkoutPlanner.fxml"); }
    @FXML private void toggleTheme() { }

    @FXML private void logout() {
        try {
            if (webSocketService != null) webSocketService.disconnect();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void attachFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            String message = "📎 Fichier attaché: " + selectedFile.getName();
            if (messageInput != null) {
                messageInput.setText(message);
                sendMessage();
            }
        }
    }

    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (IOException e) {
            System.err.println("Erreur chargement: " + fxmlPath);
            e.printStackTrace();
        }
    }

    // ==================== CLASSE INTERNE ====================

    public static class ConversationItem {
        private int conversationId;
        private String coachUuid;
        private String name;
        private String lastMessage;
        private int unreadCount;

        public ConversationItem(int id, String uuid, String name, String lastMsg, int unread) {
            this.conversationId = id;
            this.coachUuid = uuid;
            this.name = name;
            this.lastMessage = lastMsg;
            this.unreadCount = unread;
        }

        public int getConversationId() { return conversationId; }
        public String getCoachUuid() { return coachUuid; }
        public String getName() { return name; }
        public String getLastMessage() { return lastMessage; }
        public int getUnreadCount() { return unreadCount; }

        @Override
        public String toString() {
            return name + (unreadCount > 0 ? " • " + unreadCount : "");
        }
    }
}