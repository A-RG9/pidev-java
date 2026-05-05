package com.wellcare.javafx.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Service for sending emails using the SendGrid REST API.
 */
public class EmailService {

  // SendGrid API Key - loaded from environment variable (set in your OS or .env file)
  private static final String SENDGRID_API_KEY = System.getenv().getOrDefault("SENDGRID_API_KEY", "YOUR_SENDGRID_API_KEY_HERE");
  private static final String SENDGRID_ENDPOINT = "https://api.sendgrid.com/v3/mail/send";
  private static final String FROM_EMAIL = "zeidimohamedtaher@gmail.com";

  private final HttpClient httpClient;

  public EmailService() {
    this.httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .connectTimeout(Duration.ofSeconds(10))
        .build();
  }

  public boolean sendVerificationEmail(String toEmail, String token) {
    String jsonPayload = String.format(
        """
            {
              "personalizations": [
                {
                  "to": [
                    {
                      "email": "%s"
                    }
                  ],
                  "subject": "Vérifiez votre adresse email - WellCare Connect"
                }
              ],
              "from": {
                "email": "%s",
                "name": "WellCare Connect"
              },
              "content": [
                {
                  "type": "text/html",
                  "value": "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e9ecef; border-radius: 8px; padding: 20px;'><h2 style='color: #00A790; text-align: center;'>Welcome to WellCare!</h2><p>Please verify your email address to complete your registration.</p><p>Your verification code is:</p><h1 style='text-align: center; font-size: 36px; letter-spacing: 5px; color: #002F5C; background: #F5F9FF; padding: 15px; border-radius: 8px;'>%s</h1><p>This code will expire in 24 hours.</p></div>"
                }
              ]
            }
            """,
        toEmail, FROM_EMAIL, token);

    return executeSend(jsonPayload);
  }

  public boolean sendPasswordResetEmail(String toEmail, String token) {
    String jsonPayload = String.format(
        """
            {
              "personalizations": [
                {
                  "to": [
                    {
                      "email": "%s"
                    }
                  ],
                  "subject": "Réinitialisation de votre mot de passe - WellCare Connect"
                }
              ],
              "from": {
                "email": "%s",
                "name": "WellCare Security"
              },
              "content": [
                {
                  "type": "text/html",
                  "value": "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e9ecef; border-radius: 8px; padding: 20px;'><h2 style='color: #E63946; text-align: center;'>Demande de Réinitialisation</h2><p>Vous avez demandé à réinitialiser votre mot de passe WellCare Connect.</p><p>Veuillez copier le jeton de sécurité ci-dessous et le coller dans l'application pour créer votre nouveau mot de passe:</p><div style='text-align: center; margin: 20px 0;'><code style='background: #F8F9FA; border: 1px solid #DEE2E6; padding: 15px; border-radius: 4px; font-size: 14px; word-break: break-all; color: #343A40; display: block;'>%s</code></div><p style='color: #6C757D; font-size: 13px;'>Ce jeton expirera dans 2 heures. Si vous n'avez pas demandé cette réinitialisation, veuillez ignorer cet e-mail en toute sécurité.</p></div>"
                }
              ]
            }
            """,
        toEmail, FROM_EMAIL, token);

    return executeSend(jsonPayload);
  }

  public boolean sendSimpleEmail(String toEmail, String subject, String content) {
    String jsonPayload = String.format(
        """
            {
              "personalizations": [
                {
                  "to": [
                    {
                      "email": "%s"
                    }
                  ],
                  "subject": "%s"
                }
              ],
              "from": {
                "email": "%s",
                "name": "WellCare System"
              },
              "content": [
                {
                  "type": "text/plain",
                  "value": "%s"
                }
              ]
            }
            """,
        toEmail, subject, FROM_EMAIL, content);

    return executeSend(jsonPayload);
  }

  private boolean executeSend(String jsonPayload) {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(SENDGRID_ENDPOINT))
        .timeout(Duration.ofMinutes(1))
        .header("Authorization", "Bearer " + SENDGRID_API_KEY)
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
        .build();

    try {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      return response.statusCode() == 202;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean sendPasswordChangedConfirmation(String toEmail) {
    String jsonPayload = String.format(
        """
            {
              "personalizations": [
                {
                  "to": [
                    {
                      "email": "%s"
                    }
                  ],
                  "subject": "Votre mot de passe a été modifié - WellCare Connect"
                }
              ],
              "from": {
                "email": "%s",
                "name": "WellCare Security"
              },
              "content": [
                {
                  "type": "text/html",
                  "value": "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e9ecef; border-radius: 8px; padding: 20px;'><h2 style='color: #00A790; text-align: center;'>Mot de Passe Mis à Jour ✅</h2><p>Le mot de passe de votre compte WellCare a été modifié avec succès.</p><p style='padding: 10px; background: #FFF3CD; border-radius: 4px; color: #856404;'><strong>Alerte de Sécurité:</strong> Si vous n'êtes pas à l'origine de ce changement, veuillez contacter l'assistance immédiatement pour sécuriser votre compte.</p></div>"
                }
              ]
            }
            """,
        toEmail, FROM_EMAIL);

    return executeSend(jsonPayload);
  }
}
