package com.wellcare.javafx.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import com.wellcare.javafx.service.ChatbotService;
import com.wellcare.javafx.service.ChatbotServiceInterface;

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
            "Bienvenue sur l'assistant prÃ©-consultation WellCare.\n\n" +
            "Je vais vous aider Ã  :\n" +
            "â€¢ Analyser vos symptÃ´mes\n" +
            "â€¢ Classer votre Ã©tat (vert/orange/rouge)\n" +
            "â€¢ Vous orienter vers le bon spÃ©cialiste\n" +
            "â€¢ Vous donner des conseils simples en attendant\n\n" +
            "DÃ©crivez vos symptÃ´mes en quelques mots :\n" +
            "â€¢ \"J'ai mal Ã  la poitrine\"\n" +
            "â€¢ \"Je tousse depuis 3 jours\"\n" +
            "â€¢ \"J'ai mal au ventre\"\n" +
            "â€¢ \"Je suis trÃ¨s fatiguÃ©\"\n" +
            "â€¢ \"J'ai de la fiÃ¨vre\"",
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
                "âš ï¸ URGENCE DÃ‰TECTÃ‰E âš ï¸\n\n" +
                "Votre message mentionne des symptÃ´mes graves.\n\n" +
                "ðŸ”´ ACTION IMMÃ‰DIATE : Appelez le **15** (SAMU) sans attendre.\n\n" +
                "En attendant les secours :\n" +
                "â€¢ Ne prenez pas de mÃ©dicament\n" +
                "â€¢ Allongez-vous si possible\n" +
                "â€¢ Ne restez pas seul(e)",
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
        
        Text typingText = new Text("L'assistant Ã©crit...");
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

