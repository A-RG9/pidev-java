import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class TestSMTPSend {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("bagaimbus@gmail.com", "V2HBF17Q1GH6HPLCXTXZGCNJ");
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("bagaimbus@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("bagaimbus@gmail.com"));
            message.setSubject("Test From Java");
            message.setText("This is a test to verify the SMTP connection works.");
            System.out.println("Connecting to Google SMTP Servers using the provided code...");
            Transport.send(message);
            System.out.println("SUCCESS! The credentials worked and the test email was sent.");
        } catch (Exception e) {
            System.err.println("\n--- SMTP AUTHENTICATION FAILED ---");
            e.printStackTrace();
        }
    }
}
