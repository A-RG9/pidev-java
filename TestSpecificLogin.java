import com.wellcare.javafx.model.User;
import com.wellcare.javafx.service.UserService;
import com.wellcare.javafx.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestSpecificLogin {
    public static void main(String[] args) {
        System.out.println("Testing specific login: emailnn@gmail.com / 47200311BAga@");

        try {
            // First check database directly
            checkDatabaseDirectly();

            // Then test through UserService
            testUserServiceLogin();

        } catch (Exception e) {
            System.err.println("Error during testing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void checkDatabaseDirectly() throws Exception {
        System.out.println("\n=== DATABASE DIRECT CHECK ===");

        Connection conn = DatabaseConnection.getConnection();
        String query = "SELECT uuid, email, password, first_name, last_name, role, is_active, is_verified_by_admin, is_email_verified FROM users WHERE email = ?";

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, "emailnn@gmail.com");

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            System.out.println("User found in database:");
            System.out.println("UUID: " + rs.getString("uuid"));
            System.out.println("Email: " + rs.getString("email"));
            System.out.println("First Name: " + rs.getString("first_name"));
            System.out.println("Last Name: " + rs.getString("last_name"));
            System.out.println("Role: " + rs.getString("role"));
            System.out.println("Is Active: " + rs.getBoolean("is_active"));
            System.out.println("Is Verified by Admin: " + rs.getBoolean("is_verified_by_admin"));
            System.out.println("Is Email Verified: " + rs.getBoolean("is_email_verified"));
            System.out.println("Password hash: " + rs.getString("password"));
        } else {
            System.out.println("User NOT found in database");
        }

        rs.close();
        stmt.close();
        conn.close();
    }

    private static void testUserServiceLogin() throws Exception {
        System.out.println("\n=== USER SERVICE LOGIN TEST ===");

        UserService userService = new UserService();
        User user = userService.authenticate("emailnn@gmail.com", "47200311BAga@");

        if (user != null) {
            System.out.println("Login successful!");
            System.out.println("User: " + user.getFirstName() + " " + user.getLastName());
            System.out.println("Role: " + user.getRole());
            System.out.println("Is Active: " + user.isActive());
            System.out.println("Is Verified by Admin: " + user.isVerifiedByAdmin());
        } else {
            System.out.println("Login failed - user returned as null");
        }
    }
}