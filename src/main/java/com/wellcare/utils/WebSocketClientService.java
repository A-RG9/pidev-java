package com.wellcare.utils;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.URI;
import java.util.function.Consumer;

public class WebSocketClientService {

    private WebSocketClient webSocketClient;
    private boolean isConnected = false;
    private Consumer<JSONObject> onMessageReceived;
    private Consumer<String> onStatusChanged;

    public void connect(String serverUrl, String userId, String userRole,
                        Consumer<JSONObject> onMessage, Consumer<String> onStatus) {

        this.onMessageReceived = onMessage;
        this.onStatusChanged = onStatus;

        try {
            URI uri = new URI(serverUrl);
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    isConnected = true;
                    System.out.println("WebSocket connecté");

                    JSONObject authMsg = new JSONObject();
                    authMsg.put("type", "auth");
                    authMsg.put("userId", userId);
                    authMsg.put("role", userRole);
                    send(authMsg.toJSONString());

                    if (onStatusChanged != null) {
                        onStatusChanged.accept("Connecté");
                    }
                }

                @Override
                public void onMessage(String message) {
                    try {
                        JSONParser parser = new JSONParser();
                        JSONObject json = (JSONObject) parser.parse(message);
                        if (onMessageReceived != null) {
                            onMessageReceived.accept(json);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    isConnected = false;
                    System.out.println("WebSocket fermé: " + reason);
                    if (onStatusChanged != null) {
                        onStatusChanged.accept("Déconnecté");
                    }
                    // Tentative de reconnexion
                    tryReconnect(serverUrl, userId, userRole);
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("Erreur WebSocket: " + ex.getMessage());
                }
            };

            webSocketClient.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tryReconnect(String serverUrl, String userId, String userRole) {
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                System.out.println("Tentative de reconnexion...");
                connect(serverUrl, userId, userRole, onMessageReceived, onStatusChanged);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendMessage(String conversationId, String content, String senderId, String receiverId) {
        if (!isConnected || webSocketClient == null) {
            System.err.println("WebSocket non connecté, message non envoyé");
            return;
        }

        JSONObject message = new JSONObject();
        message.put("type", "message");
        message.put("conversationId", conversationId);
        message.put("content", content);
        message.put("senderId", senderId);
        message.put("receiverId", receiverId);
        message.put("timestamp", System.currentTimeMillis());

        webSocketClient.send(message.toJSONString());
        System.out.println("📤 Message envoyé: " + content);
    }

    public void sendTyping(String conversationId, String userId, boolean isTyping) {
        if (!isConnected) return;

        JSONObject typingMsg = new JSONObject();
        typingMsg.put("type", "typing");
        typingMsg.put("conversationId", conversationId);
        typingMsg.put("userId", userId);
        typingMsg.put("isTyping", isTyping);

        webSocketClient.send(typingMsg.toJSONString());
    }

    public void disconnect() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }
}