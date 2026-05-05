package com.wellcare.server;

public class WebSocketServerLauncher {

    public static void main(String[] args) {
        int port = 8887;

        // Vérifier si un port personnalisé est fourni
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Port invalide, utilisation du port par défaut 8887");
            }
        }

        SimpleWebSocketServer server = new SimpleWebSocketServer(port);
        server.start();

        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║         🚀 SERVEUR WEBSOCKET WELLORA DÉMARRÉ 🚀           ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║  📍 Adresse: ws://localhost:" + port + "                      ║");
        System.out.println("║  💬 Chat en temps réel activé                              ║");
        System.out.println("║  🎥 Support visioconférence prêt                          ║");
        System.out.println("║  👥 Gestion des statuts en ligne                          ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Appuyez sur Ctrl+C pour arrêter le serveur");
    }
}