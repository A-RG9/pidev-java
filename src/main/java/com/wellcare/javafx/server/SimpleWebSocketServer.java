package com.wellcare.javafx.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleWebSocketServer extends WebSocketServer {

    private Map<String, WebSocket> clients = new ConcurrentHashMap<>();
    private Map<String, String> userSessions = new ConcurrentHashMap<>();

    public SimpleWebSocketServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("🔌 Nouvelle connexion: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("🔌 Connexion fermée: " + conn.getRemoteSocketAddress());
        String sessionKey = conn.getRemoteSocketAddress().toString();
        String userId = userSessions.remove(sessionKey);
        if (userId != null) {
            clients.remove(userId);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(message);
            String type = (String) json.get("type");

            switch (type) {
                case "auth":
                    String userId = (String) json.get("userId");
                    String role = (String) json.get("role");
                    String sessionKey = conn.getRemoteSocketAddress().toString();
                    userSessions.put(sessionKey, userId);
                    clients.put(userId, conn);
                    System.out.println("✅ Authentifié: " + userId + " (" + role + ")");
                    break;

                case "message":
                    String receiverId = (String) json.get("receiverId");
                    WebSocket receiverSocket = clients.get(receiverId);
                    if (receiverSocket != null && receiverSocket.isOpen()) {
                        receiverSocket.send(message);
                    }
                    break;

                case "typing":
                    // Forward typing status to the other participant
                    String typingUserId = (String) json.get("userId");
                    for (Map.Entry<String, WebSocket> entry : clients.entrySet()) {
                        if (!entry.getKey().equals(typingUserId)) {
                            entry.getValue().send(message);
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("❌ Erreur WebSocket: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("🚀 Serveur WebSocket démarré sur le port " + getPort());
    }
}