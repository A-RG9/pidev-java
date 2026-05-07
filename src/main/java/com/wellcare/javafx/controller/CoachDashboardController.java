package com.wellcare.javafx.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import com.wellcare.javafx.util.Database;
import com.wellcare.javafx.util.JitsiService;
import com.wellcare.javafx.util.WebSocketClientService;
import org.json.simple.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class CoachDashboardController {

    // ==================== FXML COMPOSANTS ====================
    @FXML private Label coachName, coachEmail;
    @FXML private Label lblConnectionStatus;
    @FXML private Label selectedClientName;
    @FXML private Label lblClientStatus;
    @FXML private Label lblTypingIndicator;
    @FXML private TextField searchConversation, messageInput;
    @FXML private ListView<ConversationItem> conversationsList;
    @FXML private VBox messagesContainer;
    @FXML private ScrollPane messagesScrollPane;
    @FXML private Label lblUnreadCount;
    @FXML private Label lblUnreadCountStat;
    @FXML private Label lblOnlineClients;
    @FXML private Label lblTotalConversations;
    @FXML private Button btnStartCall;
    @FXML private Button btnJoinCall;
    @FXML private VBox videoCallOverlay;
    @FXML private WebView videoWebView;
    @FXML private Label lblCallStatus;
    @FXML private Label lblCallDuration;

    // ==================== VARIABLES ====================
    private String currentCoachUuid;
    private String currentCoachName;
    private String currentPatientUuid;
    private String currentPatientName;
    private int currentConversationId;

    private WebSocketClientService webSocketService;
    private boolean isTyping = false;
    private Timer typingTimer;
    private JitsiService jitsiService = new JitsiService();

    // ==================== INITIALISATION ====================
    @FXML
    public void initialize() {
        loadCoachInfo();
        loadConversations();
        setupSearchListener();
        setupTypingDetection();
        connectWebSocket();
        startOnlineStatusChecker();

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
        if (currentCoachUuid == null) return;
        webSocketService = new WebSocketClientService();
        webSocketService.connect(
                "ws://localhost:8887",
                currentCoachUuid,
                "coach",
                this::onWebSocketMessage,
                this::onWebSocketStatus
        );
    }

    private void onWebSocketMessage(JSONObject message) {
        Platform.runLater(() -> {
            String type = (String) message.get("type");
            if ("message".equals(type)) {
                String content = (String) message.get("content");
                String senderId = (String) message.get("senderId");
                if (senderId != null && senderId.equals(currentPatientUuid)) {
                    addMessageToChat(content, LocalDateTime.now(), false);
                    markMessagesAsRead(currentConversationId);
                }
            } else if ("typing".equals(type)) {
                String userId = (String) message.get("userId");
                boolean typing = (boolean) message.get("isTyping");
                if (userId != null && userId.equals(currentPatientUuid)) {
                    showTypingIndicator(typing);
                }
            } else if ("status".equals(type)) {
                String statusUserId = (String) message.get("userId");
                String status = (String) message.get("status");
                if (statusUserId != null && statusUserId.equals(currentPatientUuid)) {
                    updateClientStatus(status);
                }
            }
        });
    }

    private void onWebSocketStatus(String status) {
        Platform.runLater(() -> {
            if (lblConnectionStatus != null) {
                lblConnectionStatus.setText(status.contains("Connecté") ? "🟢" : "🔴");
            }
        });
    }

    private void sendTypingStatus(boolean typing) {
        if (webSocketService != null && webSocketService.isConnected() && currentConversationId > 0 && currentPatientUuid != null) {
            webSocketService.sendTyping(String.valueOf(currentConversationId), currentCoachUuid, typing);
        }
    }

    private void showTypingIndicator(boolean typing) {
        Platform.runLater(() -> {
            if (lblTypingIndicator != null) {
                lblTypingIndicator.setText(typing ? "👤 Le client est en train d'écrire..." : "");
            }
        });
    }

    private void updateClientStatus(String status) {
        Platform.runLater(() -> {
            if (lblClientStatus != null) {
                if (status.equals("online")) {
                    lblClientStatus.setText("🟢 En ligne");
                    lblClientStatus.setStyle("-fx-text-fill: #10B981;");
                } else {
                    lblClientStatus.setText("⚫ Hors ligne");
                    lblClientStatus.setStyle("-fx-text-fill: #94A3B8;");
                }
            }
        });
    }

    // ==================== CHARGEMENT DES DONNÉES ====================

    private void loadCoachInfo() {
        com.wellcare.javafx.model.User currentUser = com.wellcare.javafx.util.SceneManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentCoachUuid = currentUser.getUuid();
            currentCoachName = currentUser.getFirstName() + " " + currentUser.getLastName();
            if (coachName != null) coachName.setText(currentCoachName);
            if (coachEmail != null) coachEmail.setText(currentUser.getEmail());
        } else {
            // Fallback for development/testing if no user is logged in
            try (Connection conn = Database.getConnection()) {
                String sql = "SELECT uuid, first_name, last_name, email FROM users WHERE role = 'ROLE_COACH' LIMIT 1";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    currentCoachUuid = rs.getString("uuid");
                    currentCoachName = rs.getString("first_name") + " " + rs.getString("last_name");
                    if (coachName != null) coachName.setText(currentCoachName);
                    if (coachEmail != null) coachEmail.setText(rs.getString("email"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadConversations() {
        if (conversationsList == null) return;
        conversationsList.getItems().clear();

        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT c.id as conversation_id, u.uuid as patient_uuid, u.first_name, u.last_name, " +
                    "(SELECT content FROM message WHERE conversation_id = c.id ORDER BY sent_at DESC LIMIT 1) as last_message, " +
                    "(SELECT COUNT(*) FROM message WHERE conversation_id = c.id AND is_read = 0 AND sender_uuid != ?) as unread_count " +
                    "FROM conversation c JOIN users u ON c.patient_uuid = u.uuid WHERE c.coach_uuid = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, currentCoachUuid);
            pstmt.setString(2, currentCoachUuid);
            ResultSet rs = pstmt.executeQuery();

            int totalUnread = 0;
            while (rs.next()) {
                ConversationItem conv = new ConversationItem(
                        rs.getInt("conversation_id"),
                        rs.getString("patient_uuid"),
                        rs.getString("first_name") + " " + rs.getString("last_name"),
                        rs.getString("last_message"),
                        rs.getInt("unread_count")
                );
                conversationsList.getItems().add(conv);
                totalUnread += rs.getInt("unread_count");
            }
            if (lblUnreadCount != null) lblUnreadCount.setText(String.valueOf(totalUnread));
            if (lblUnreadCountStat != null) lblUnreadCountStat.setText(String.valueOf(totalUnread));
            if (lblTotalConversations != null) lblTotalConversations.setText(String.valueOf(conversationsList.getItems().size()));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        conversationsList.setOnMouseClicked(event -> {
            ConversationItem selected = conversationsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                currentPatientUuid = selected.getPatientUuid();
                currentPatientName = selected.getName();
                currentConversationId = selected.getConversationId();
                if (selectedClientName != null) selectedClientName.setText(currentPatientName);
                loadMessages(currentConversationId);
                markMessagesAsRead(currentConversationId);
                if (btnStartCall != null) btnStartCall.setDisable(false);
                if (btnJoinCall != null) btnJoinCall.setDisable(false);
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
            pstmt.setString(3, currentCoachUuid);
            pstmt.executeUpdate();
            loadConversations();
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
            pstmt.setString(4, currentCoachUuid);
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
            webSocketService.sendMessage(String.valueOf(currentConversationId), message, currentCoachUuid, currentPatientUuid);
        }

        addMessageToChat(message, LocalDateTime.now(), true);
        messageInput.clear();
        loadConversations();
    }

    private void addMessageToChat(String message, LocalDateTime time, boolean isFromCoach) {
        if (messagesContainer == null) return;
        HBox messageRow = new HBox();
        messageRow.setAlignment(isFromCoach ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        VBox messageBubble = new VBox(3);

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
            String bubbleStyle = isFromCoach ? "-fx-background-color: #14b8a6; -fx-background-radius: 15; -fx-padding: 8 12;" : "-fx-background-color: #f1f5f9; -fx-background-radius: 15; -fx-padding: 8 12;";
            messageBubble.setStyle(bubbleStyle);
            Label messageLabel = new Label(message);
            messageLabel.setStyle("-fx-text-fill: " + (isFromCoach ? "white" : "#1e293b") + "; -fx-wrap-text: true;");
            messageLabel.setMaxWidth(400);
            Label timeLabel = new Label(time.format(DateTimeFormatter.ofPattern("HH:mm")));
            timeLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px;");
            messageBubble.getChildren().addAll(messageLabel, timeLabel);
        }

        messageRow.getChildren().add(messageBubble);
        messageRow.setStyle("-fx-padding: 5;");
        messagesContainer.getChildren().add(messageRow);
        if (messagesScrollPane != null) messagesScrollPane.setVvalue(1.0);
    }

    // ==================== VISIOCONFÉRENCE ====================

    @FXML
    private void startVideoCall() {
        if (currentPatientUuid == null) {
            showAlert("Veuillez sélectionner un client d'abord");
            return;
        }

        String meetingUrl = jitsiService.createPrivateMeeting(currentCoachUuid, currentPatientUuid);
        String linkMessage = "🔗 Session vidéo: " + meetingUrl;

        // 1. Ouvrir le lien pour le coach
        openInBrowser(meetingUrl);

        // 2. Sauvegarder dans la base de données
        try (Connection conn = Database.getConnection()) {
            String sql = "INSERT INTO message (content, sent_at, is_read, conversation_id, sender_uuid) VALUES (?, ?, 0, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, linkMessage);
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(3, currentConversationId);
            pstmt.setString(4, currentCoachUuid);
            pstmt.executeUpdate();

            String updateSql = "UPDATE conversation SET last_message_at = ? WHERE id = ?";
            PreparedStatement pstmt2 = conn.prepareStatement(updateSql);
            pstmt2.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt2.setInt(2, currentConversationId);
            pstmt2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 3. Envoyer via WebSocket
        if (webSocketService != null && webSocketService.isConnected()) {
            webSocketService.sendMessage(
                    String.valueOf(currentConversationId),
                    linkMessage,
                    currentCoachUuid,
                    currentPatientUuid
            );
        }

        // 4. Afficher dans le chat
        addMessageToChat(linkMessage, LocalDateTime.now(), true);
        showSuccessMessage("Session vidéo démarrée ! Le lien a été envoyé au patient.");
    }

    @FXML
    private void joinVideoCall() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Rejoindre un appel");
        dialog.setHeaderText("Entrez le lien de la réunion");
        dialog.setContentText("Lien Jitsi Meet:");

        dialog.showAndWait().ifPresent(url -> {
            if (!url.isEmpty()) {
                openInBrowser(url);
            }
        });
    }

    private void openInBrowser(String url) {
        final String finalUrl = url;
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(finalUrl));
            } else {
                showAlert("Copiez ce lien dans votre navigateur: " + finalUrl);
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            showAlert("Erreur: " + e.getMessage());
        }
    }

    private void addSystemMessageToChat(String message) {
        if (messagesContainer == null) return;

        HBox messageRow = new HBox();
        messageRow.setAlignment(Pos.CENTER);

        final String finalMessage = message;
        Hyperlink link = new Hyperlink(message);
        link.setStyle("-fx-text-fill: #3b82f6; -fx-underline: true; -fx-font-size: 11px; -fx-cursor: hand;");
        link.setOnAction(e -> {
            String url = finalMessage.replace("🔗 Session vidéo: ", "");
            openInBrowser(url);
        });

        messageRow.getChildren().add(link);
        messagesContainer.getChildren().add(messageRow);
        if (messagesScrollPane != null) messagesScrollPane.setVvalue(1.0);
    }

    @FXML
    private void closeVideoCall() {
        if (videoCallOverlay != null) videoCallOverlay.setVisible(false);
    }

    @FXML private void toggleMicro() { }
    @FXML private void toggleCamera() { }
    @FXML private void shareScreen() { }

    private void setupSearchListener() {
        if (searchConversation != null && conversationsList != null) {
            searchConversation.textProperty().addListener((obs, old, val) -> {
                conversationsList.setItems(conversationsList.getItems().filtered(
                        conv -> conv.getName().toLowerCase().contains(val.toLowerCase())
                ));
            });
        }
    }

    private void startOnlineStatusChecker() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (lblOnlineClients != null) {
                        lblOnlineClients.setText(String.valueOf(conversationsList.getItems().size()));
                    }
                });
            }
        }, 0, 30000);
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
    @FXML private void loadDashboard() { System.out.println("Dashboard"); }
    @FXML private void loadClients() { System.out.println("Clients"); }
    @FXML private void loadExerciseLibrary() { System.out.println("Exercices"); }
    @FXML private void loadPrograms() { System.out.println("Programmes"); }
    @FXML private void loadMessages() { }
    @FXML private void loadVideoCall() { startVideoCall(); }
    @FXML private void loadAICoach() { System.out.println("AI Coach"); }
    @FXML private void toggleTheme() { }
    @FXML private void logout() {
        if (webSocketService != null) webSocketService.disconnect();
        System.exit(0);
    }
    @FXML private void attachFile() { }

    // ==================== CLASSE INTERNE ====================
    public static class ConversationItem {
        private int conversationId;
        private String patientUuid;
        private String name;
        private String lastMessage;
        private int unreadCount;

        public ConversationItem(int id, String uuid, String name, String lastMsg, int unread) {
            this.conversationId = id;
            this.patientUuid = uuid;
            this.name = name;
            this.lastMessage = lastMsg;
            this.unreadCount = unread;
        }

        public int getConversationId() { return conversationId; }
        public String getPatientUuid() { return patientUuid; }
        public String getName() { return name; }
        public String getLastMessage() { return lastMessage; }
        public int getUnreadCount() { return unreadCount; }
        @Override public String toString() { return name + (unreadCount > 0 ? " • " + unreadCount : ""); }
    }
}