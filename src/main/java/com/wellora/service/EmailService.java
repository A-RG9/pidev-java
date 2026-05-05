package com.wellora.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    // Mailtrap SMTP configuration (replace with your own if needed)
    private static final String HOST = "sandbox.smtp.mailtrap.io";
    private static final int PORT = 2525;
    private static final String USERNAME = "02a0c185ac236a";
    private static final String PASSWORD = "7304c64ab0e268";
    private static final boolean TLS = true;
    private static final boolean SSL = false;

    /**
     * Sends an email for accepted appointment.
     *
     * @param patientEmail   recipient email address
     * @param patientName    patient's full name
     * @param doctorName     doctor's name
     * @param appointmentDate date (dd/MM/yyyy)
     * @param appointmentTime time (HH:mm)
     */
    public static void sendAppointmentAcceptedEmail(String patientEmail, String patientName,
                                                    String doctorName, String appointmentDate,
                                                    String appointmentTime) {
        // SMTP server properties
        Properties props = new Properties();
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", TLS);
        props.put("mail.smtp.ssl.enable", SSL);

        // Authenticator with Mailtrap credentials
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        };

        // Create mail session
        Session session = Session.getInstance(props, auth);
        // For debugging, uncomment the next line:
        // session.setDebug(true);

        try {
            // Build email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("noreply@wellcare.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(patientEmail));
            message.setSubject("Appointment Accepted – WellCare Connect");

            // HTML content
            String htmlContent = "<html><body>"
                    + "<h2>Appointment Accepted</h2>"
                    + "<p>Hello " + patientName + ",</p>"
                    + "<p>Your appointment request has been accepted by Dr. " + doctorName + ".</p>"
                    + "<p><strong>Appointment Details:</strong></p>"
                    + "<ul>"
                    + "<li>Date: " + appointmentDate + "</li>"
                    + "<li>Time: " + appointmentTime + "</li>"
                    + "<li>Doctor: Dr. " + doctorName + "</li>"
                    + "</ul>"
                    + "<p>Please be available at the scheduled time.</p>"
                    + "<p>Thank you for using WellCare Connect!</p>"
                    + "</body></html>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            // Send
            Transport.send(message);
            System.out.println("Email sent successfully to " + patientEmail);

        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }

    }
}