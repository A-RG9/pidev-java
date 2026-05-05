import com.wellcare.javafx.model.User;
import com.wellcare.javafx.service.UserService;
import com.wellcare.javafx.util.SceneManager;
import com.wellcare.javafx.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestAdminDashboard {
    public static void main(String[] args) {
        try {
            // Test database connection
            Connection conn = DatabaseConnection.getInstance().getConnection();
            if (conn != null) {
                System.out.println("✅ Database connection successful");
            }

            // Test admin login
            UserService userService = new UserService();

            // Test admin authentication
            System.out.println("\n🔍 Testing Admin Authentication...");
            String[][] adminCredentials = {
                {"admin@wellcare.com", "Admin123"},
                {"admin@wellcare.tn", "admin123"},
                {"admin123@gmail.com", "admin123"}
            };

            User adminUser = null;
            String workingEmail = null;
            String workingPassword = null;

            for (String[] creds : adminCredentials) {
                try {
                    String email = creds[0];
                    String password = creds[1];

                    System.out.println("Testing: " + email + " / " + password);

                    User user = userService.authenticate(email, password);
                    if (user != null) {
                        System.out.println("✅ Authentication successful!");
                        System.out.println("   Role: " + user.getRole());
                        System.out.println("   Active: " + user.isActive());
                        System.out.println("   Verified: " + user.isVerifiedByAdmin());

                        if ("ROLE_ADMIN".equals(user.getRole()) && user.isActive()) {
                            adminUser = user;
                            workingEmail = email;
                            workingPassword = password;
                            break;
                        }
                    } else {
                        System.out.println("❌ Authentication failed");
                    }
                } catch (Exception e) {
                    System.out.println("❌ Error testing " + creds[0] + ": " + e.getMessage());
                }
            }

            if (adminUser != null) {
                System.out.println("\n🎯 Working Admin Credentials Found:");
                System.out.println("   Email: " + workingEmail);
                System.out.println("   Password: " + workingPassword);
                System.out.println("   Role: " + adminUser.getRole());

                // Test dashboard routing
                System.out.println("\n🏥 Testing Admin Dashboard Routing...");
                System.out.println("   Expected route: " + SceneManager.ADMIN_DASHBOARD);
                System.out.println("   User role: " + adminUser.getRole());

                // Check database statistics
                System.out.println("\n📊 Testing Database Statistics...");
                testDatabaseStats();

            } else {
                System.out.println("\n❌ No working admin credentials found!");
            }

        } catch (Exception e) {
            System.err.println("❌ Error testing admin dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testDatabaseStats() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT role, COUNT(*) as count FROM users GROUP BY role";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            System.out.println("📈 User Statistics by Role:");
            while (rs.next()) {
                String role = rs.getString("role");
                int count = rs.getInt("count");
                System.out.println("   " + role + ": " + count);
            }

            // Check pending verifications
            String pendingQuery = "SELECT COUNT(*) as pending FROM users WHERE is_verified_by_admin = 0 AND role IN ('ROLE_MEDECIN', 'ROLE_COACH', 'ROLE_NUTRITIONIST')";
            PreparedStatement pendingStmt = conn.prepareStatement(pendingQuery);
            ResultSet pendingRs = pendingStmt.executeQuery();

            if (pendingRs.next()) {
                int pendingCount = pendingRs.getInt("pending");
                System.out.println("   Pending Verifications: " + pendingCount);
            }

            rs.close();
            stmt.close();
            pendingRs.close();
            pendingStmt.close();
            conn.close();

        } catch (Exception e) {
            System.err.println("❌ Error testing database stats: " + e.getMessage());
        }
    }
}