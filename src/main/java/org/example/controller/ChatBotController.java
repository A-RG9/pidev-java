package org.example.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.example.service.ChatbotService;
import org.example.service.ChatbotServiceInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the AI Chatbot interface
 */
public class ChatBotController {
    
    @FXML
    private VBox messagesContainer;
    
    @FXML
    private TextField userInput;
    
    @FXML
    private Button sendButton;
    
    @FXML
    private ScrollPane scrollPane;
    
    private ChatbotServiceInterface chatbotService;
    private List<ChatbotServiceInterface.Message> conversationHistory;
    
    @FXML
    public void initialize() {
        chatbotService = new ChatbotService();
        conversationHistory = new ArrayList<>();
        
        // Add welcome message
        addBotMessage(
            "Bienvenue sur l'assistant pré-consultation WellCare.\n\n" +
            "Je vais vous aider à :\n" +
            "• Analyser vos symptômes\n" +
            "• Classer votre état (vert/orange/rouge)\n" +
            "• Vous orienter vers le bon spécialiste\n" +
            "• Vous donner des conseils simples en attendant\n\n" +
            "Décrivez vos symptômes en quelques mots :\n" +
            "• \"J'ai mal à la poitrine\"\n" +
            "• \"Je tousse depuis 3 jours\"\n" +
            "• \"J'ai mal au ventre\"\n" +
            "• \"Je suis très fatigué\"\n" +
            "• \"J'ai de la fièvre\"",
            "info"
        );
    }
    
    @FXML
    public void handleSendMessage() {
        String message = userInput.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        
        // Check for emergency
        if (chatbotService.detectEmergency(message)) {
            addBotMessage(
                "⚠️ URGENCE DÉTECTÉE ⚠️\n\n" +
                "Votre message mentionne des symptômes graves.\n\n" +
                "🔴 ACTION IMMÉDIATE : Appelez le **15** (SAMU) sans attendre.\n\n" +
                "En attendant les secours :\n" +
                "• Ne prenez pas de médicament\n" +
                "• Allongez-vous si possible\n" +
                "• Ne restez pas seul(e)",
                "rouge"
            );
            userInput.clear();
            return;
        }
        
        // Add user message to UI
        addUserMessage(message);
        
        // Add to history
        conversationHistory.add(new ChatbotServiceInterface.Message("user", message));
        
        // Clear input
        userInput.clear();
        
        // Disable input while processing
        userInput.setDisable(true);
        sendButton.setDisable(true);
        
        // Show typing indicator
        showTypingIndicator();
        
        // Process in background thread
        new Thread(() -> {
            ChatbotServiceInterface.ChatResponse response = 
                chatbotService.generateMedicalResponse(message, conversationHistory);
            
            Platform.runLater(() -> {
                removeTypingIndicator();
                
                if (response.isSuccess()) {
                    // Add bot response to UI
                    addBotMessage(response.getMessage(), response.getLevel());
                    
                    // Add to history
                    conversationHistory.add(new ChatbotServiceInterface.Message("assistant", response.getMessage()));
                } else {
                    addBotMessage(response.getMessage(), "info");
                }
                
                // Re-enable input
                userInput.setDisable(false);
                sendButton.setDisable(false);
                userInput.requestFocus();
            });
        }).start();
    }
    
    @FXML
    public void handleReset() {
        conversationHistory.clear();
        messagesContainer.getChildren().clear();
        initialize();
    }
    
    @FXML
    public void handleSuggestionClick(javafx.event.ActionEvent event) {
        if (event.getSource() instanceof Button) {
            Button clickedButton = (Button) event.getSource();
            String suggestion = clickedButton.getText();
            userInput.setText(suggestion);
            handleSendMessage();
        }
    }
    
    private void addUserMessage(String text) {
        HBox messageBox = new HBox();
        messageBox.getStyleClass().add("message");
        messageBox.setStyle("-justify-content: flex-end;");
        
        Label messageLabel = new Label(text);
        messageLabel.getStyleClass().add("user-message");
        messageLabel.setStyle(
            "-fx-background-color: #1d4ed8; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 10px 16px; " +
            "-fx-background-radius: 16px; " +
            "-fx-font-size: 14px; " +
            "-fx-max-width: 70%;"
        );
        
        messageBox.getChildren().add(messageLabel);
        messagesContainer.getChildren().add(messageBox);
        
        scrollToBottom();
    }
    
    private void addBotMessage(String text, String level) {
        HBox messageBox = new HBox();
        messageBox.getStyleClass().add("message");
        messageBox.setStyle("-justify-content: flex-start;");
        
        VBox contentBox = new VBox();
        contentBox.getStyleClass().add("bot-message");
        
        // Set style based on level
        String borderColor;
        String bgColor;
        String textColor;
        
        switch (level) {
            case "rouge":
                borderColor = "#ef4444";
                bgColor = "#fee2e2";
                textColor = "#b91c1c";
                break;
            case "orange":
                borderColor = "#f97316";
                bgColor = "#ffedd5";
                textColor = "#c2410c";
                break;
            case "vert":
                borderColor = "#22c55e";
                bgColor = "#dcfce7";
                textColor = "#166534";
                break;
            default:
                borderColor = "#3b82f6";
                bgColor = "#dbeafe";
                textColor = "#1e3a8a";
        }
        
        contentBox.setStyle(
            "-fx-background-color: " + bgColor + "; " +
            "-fx-border-color: " + borderColor + "; " +
            "-fx-border-width: 0 0 0 6px; " +
            "-fx-padding: 12px 16px; " +
            "-fx-background-radius: 0 16px 16px 16px; " +
            "-fx-max-width: 70%;"
        );
        
        // Convert markdown-like formatting to JavaFX Text
        Text textNode = new Text(text);
        textNode.setStyle("-fx-fill: " + textColor + "; -fx-font-size: 14px;");
        textNode.setWrappingWidth(400);
        
        contentBox.getChildren().add(textNode);
        messageBox.getChildren().add(contentBox);
        messagesContainer.getChildren().add(messageBox);
        
        scrollToBottom();
    }
    
    private void showTypingIndicator() {
        HBox indicatorBox = new HBox();
        indicatorBox.setId("typingIndicator");
        indicatorBox.getStyleClass().add("message");
        indicatorBox.setStyle("-justify-content: flex-start;");
        
        VBox indicator = new VBox();
        indicator.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #e5e7eb; " +
            "-fx-border-width: 1px; " +
            "-fx-padding: 12px 16px; " +
            "-fx-background-radius: 16px;"
        );
        
        Text typingText = new Text("L'assistant écrit...");
        typingText.setStyle("-fx-fill: #6b7280; -fx-font-size: 12px;");
        indicator.getChildren().add(typingText);
        
        indicatorBox.getChildren().add(indicator);
        messagesContainer.getChildren().add(indicatorBox);
        
        scrollToBottom();
    }
    
    private void removeTypingIndicator() {
        messagesContainer.getChildren().removeIf(node -> 
            node.getId() != null && node.getId().equals("typingIndicator")
        );
    }
    
    private void scrollToBottom() {
        Platform.runLater(() -> {
            if (scrollPane != null) {
                scrollPane.setVvalue(1.0);
            }
        });
    }
    
    @FXML
    public void handleKeyPress(javafx.scene.input.KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
            handleSendMessage();
        }
    }
}
