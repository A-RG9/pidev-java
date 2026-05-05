import com.wellcare.javafx.service.UserService;
import com.wellcare.javafx.model.User;

public class TestAdminLogin {
    public static void main(String[] args) {
        try {
            UserService userService = new UserService();

            // Test different admin accounts
            String[][] adminTests = {
                {"admin@wellcare.com", "Admin123"},
                {"admin@wellcare.tn", "admin123"},
                {"admin123@gmail.com", "admin123"}
            };

            for (String[] test : adminTests) {
                String email = test[0];
                String password = test[1];

                try {
                    User user = userService.authenticate(email, password);
                    if (user != null) {
                        System.out.println("✅ SUCCESS: " + email + " can login as " + user.getRole());
                    } else {
                        System.out.println("❌ FAILED: " + email + " with password '" + password + "' - Invalid credentials");
                    }
                } catch (Exception e) {
                    System.out.println("❌ ERROR: " + email + " - " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error testing admin login: " + e.getMessage());
            e.printStackTrace();
        }
    }
}