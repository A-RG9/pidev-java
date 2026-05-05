package com.wellcare.javafx.service;

import com.wellcare.javafx.util.AppConfig;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service to handle Google OAuth 2.0 flow for Desktop Applications.
 */
public class GoogleAuthService {
    
    // Google credentials loaded securely from config.properties
    private static final String CLIENT_ID = AppConfig.getGoogleClientId();
    private static final String CLIENT_SECRET = AppConfig.getGoogleClientSecret();
    
    private static final String REDIRECT_URI = "http://127.0.0.1:8888";
    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
    
    private HttpServer server;
    private CompletableFuture<String> authCodeFuture;

    /**
     * Starts the OAuth login process.
     * @return a CompletableFuture containing the user's profile info as a Map
     */
    public CompletableFuture<Map<String, String>> login() {
        authCodeFuture = new CompletableFuture<>();
        
        try {
            startLocalServer();
            
            String url = AUTH_URL + "?" +
                    "client_id=" + CLIENT_ID +
                    "&redirect_uri=" + REDIRECT_URI +
                    "&response_type=code" +
                    "&scope=" + URLEncoder.encode("email profile", StandardCharsets.UTF_8) +
                    "&prompt=consent";
            
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                throw new IOException("Desktop browsing not supported. Please open this URL manually: " + url);
            }
            
            return authCodeFuture.thenCompose(this::exchangeCodeForToken)
                                .thenCompose(this::fetchUserInfo)
                                .whenComplete((res, ex) -> stopLocalServer());
            
        } catch (Exception e) {
            CompletableFuture<Map<String, String>> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            stopLocalServer();
            return failed;
        }
    }

    private void startLocalServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8888), 0);
        server.createContext("/", new CallbackHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Local callback server started at http://127.0.0.1:8888");
        
        // Timeout after 2 minutes
        CompletableFuture.delayedExecutor(2, TimeUnit.MINUTES).execute(() -> {
            if (!authCodeFuture.isDone()) {
                authCodeFuture.completeExceptionally(new Exception("Login timed out."));
            }
        });
    }

    private void stopLocalServer() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    private class CallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);
            
            String response;
            if (params.containsKey("code")) {
                authCodeFuture.complete(params.get("code"));
                response = "<html><body style='font-family: sans-serif; text-align: center; padding-top: 50px;'>" +
                           "<h1 style='color: #2e7d32;'>Authentication Successful!</h1>" +
                           "<p>You can now close this window and return to WellCare Connect.</p>" +
                           "</body></html>";
            } else {
                authCodeFuture.completeExceptionally(new Exception("Authorization failed: " + query));
                response = "<html><body style='font-family: sans-serif; text-align: center; padding-top: 50px;'>" +
                           "<h1 style='color: #d32f2f;'>Authentication Failed</h1>" +
                           "<p>Please try again.</p>" +
                           "</body></html>";
            }
            
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1) {
                    result.put(pair[0], pair[pair.length - 1]);
                }
            }
        }
        return result;
    }

    private CompletableFuture<String> exchangeCodeForToken(String code) {
        HttpClient client = HttpClient.newHttpClient();
        
        String body = "code=" + code +
                "&client_id=" + CLIENT_ID +
                "&client_secret=" + CLIENT_SECRET +
                "&redirect_uri=" + REDIRECT_URI +
                "&grant_type=authorization_code";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    JSONObject json = new JSONObject(response.body());
                    if (json.has("access_token")) {
                        return json.getString("access_token");
                    } else {
                        throw new RuntimeException("Failed to get access token: " + response.body());
                    }
                });
    }

    private CompletableFuture<Map<String, String>> fetchUserInfo(String accessToken) {
        HttpClient client = HttpClient.newHttpClient();
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(USER_INFO_URL))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    JSONObject json = new JSONObject(response.body());
                    Map<String, String> profile = new HashMap<>();
                    profile.put("sub", json.optString("sub"));
                    profile.put("email", json.optString("email"));
                    profile.put("given_name", json.optString("given_name"));
                    profile.put("family_name", json.optString("family_name"));
                    profile.put("picture", json.optString("picture"));
                    return profile;
                });
    }
}
