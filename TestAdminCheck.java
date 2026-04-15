import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestAdminCheck {
    public static void main(String[] args) {
        try {
            // Connect to database
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/wellora?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                "root",
                ""
            );

            // Check if admin user exists
            String query = "SELECT email, password, role FROM users WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, "admin@wellcare.com");
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("Admin user found:");
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("Password hash: " + rs.getString("password"));
                System.out.println("Role: " + rs.getString("role"));
            } else {
                System.out.println("No admin user found in database");
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}