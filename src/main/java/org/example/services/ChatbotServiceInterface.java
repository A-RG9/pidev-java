package org.example.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interface for chatbot services providing medical assistance
 */
public interface ChatbotServiceInterface {
    
    /**
     * Represents a message in the conversation
     */
     class Message {
         private String role;
         private String content;
         
         public Message(String role, String content) {
             this.role = role;
             this.content = content;
         }
         
         public String getRole() { return role; }
         public String getContent() { return content; }
         
         public Map<String, String> toMap() {
             Map<String, String> map = new HashMap<>();
             map.put("role", role);
             map.put("content", content);
             return map;
         }
     }
    
    /**
     * Response from the chatbot
     */
    class ChatResponse {
        private boolean success;
        private String message;
        private String level;
        private String specialist;
        
        public ChatResponse(boolean success, String message, String level, String specialist) {
            this.success = success;
            this.message = message;
            this.level = level;
            this.specialist = specialist;
        }
        
        public static ChatResponse success(String message, String level, String specialist) {
            return new ChatResponse(true, message, level, specialist);
        }
        
        public static ChatResponse error(String message) {
            return new ChatResponse(false, message, "info", null);
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getLevel() { return level; }
        public String getSpecialist() { return specialist; }
    }
    
    /**
     * Generate a medical response using AI
     * 
     * @param userMessage The user's message
     * @param conversationHistory Previous conversation messages
     * @return ChatResponse with the AI's answer
     */
    ChatResponse generateMedicalResponse(String userMessage, List<Message> conversationHistory);
    
    /**
     * Detect emergency keywords in user message
     * 
     * @param message The user's message
     * @return true if emergency detected, false otherwise
     */
    boolean detectEmergency(String message);
}
