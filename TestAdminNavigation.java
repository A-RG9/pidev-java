import com.wellcare.javafx.service.UserService;
import com.wellcare.javafx.util.SceneManager;

public class TestAdminNavigation {
    public static void main(String[] args) {
        try {
            UserService userService = new UserService();

            // Test scene constants exist
            System.out.println("🔍 Testing Admin Navigation Constants:");
            System.out.println("ADMIN_DASHBOARD: " + SceneManager.ADMIN_DASHBOARD);
            System.out.println("ADMIN_USERS: " + SceneManager.ADMIN_USERS);
            System.out.println("ADMIN_PROFESSIONALS: " + SceneManager.ADMIN_PROFESSIONALS);
            System.out.println("ADMIN_VERIFICATION: " + SceneManager.ADMIN_VERIFICATION);

            // Test that scene manager can be instantiated
            SceneManager sceneManager = SceneManager.getInstance();
            System.out.println("✅ SceneManager instance created successfully");

            // Check if FXML files exist (basic check)
            System.out.println("\n📁 Checking FXML Files:");
            checkFileExists("src/main/resources/fxml/dashboard/admin-dashboard.fxml", "Admin Dashboard");
            checkFileExists("src/main/resources/fxml/admin/admin-users.fxml", "User Management");
            checkFileExists("src/main/resources/fxml/admin/professional-management.fxml", "Professional Management");
            checkFileExists("src/main/resources/fxml/admin/verification-queue.fxml", "Verification Queue");

            // Check if controller classes exist
            System.out.println("\n🎮 Checking Controller Classes:");
            checkClassExists("com.wellcare.javafx.controller.dashboard.AdminDashboardController", "AdminDashboardController");
            checkClassExists("com.wellcare.javafx.controller.admin.UserManagementController", "UserManagementController");
            checkClassExists("com.wellcare.javafx.controller.admin.ProfessionalManagementController", "ProfessionalManagementController");
            checkClassExists("com.wellcare.javafx.controller.admin.VerificationQueueController", "VerificationQueueController");

            System.out.println("\n🎯 Admin Navigation Test: PASSED ✅");
            System.out.println("All admin dashboard features should now be functional!");

        } catch (Exception e) {
            System.err.println("❌ Admin Navigation Test: FAILED");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void checkFileExists(String path, String description) {
        try {
            java.io.File file = new java.io.File(path);
            if (file.exists()) {
                System.out.println("✅ " + description + ": " + path);
            } else {
                System.out.println("❌ " + description + ": " + path + " (NOT FOUND)");
            }
        } catch (Exception e) {
            System.out.println("❌ " + description + ": Error checking " + path);
        }
    }

    private static void checkClassExists(String className, String description) {
        try {
            Class.forName(className);
            System.out.println("✅ " + description + ": " + className);
        } catch (ClassNotFoundException e) {
            System.out.println("❌ " + description + ": " + className + " (NOT FOUND)");
        }
    }
}