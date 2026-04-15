import com.wellcare.javafx.model.User;
import com.wellcare.javafx.service.UserService;
import java.time.LocalDateTime;

public class CreateAdmin {
    public static void main(String[] args) {
        try {
            UserService userService = new UserService();

            // Try to check if admin exists (but don't fail if not)
            try {
                User existingAdmin = userService.authenticate("admin@wellcare.com", "Admin123");
                if (existingAdmin != null) {
                    System.out.println("Admin user already exists!");
                    return;
                }
            } catch (Exception e) {
                // Admin doesn't exist, continue to create
                System.out.println("Admin user not found, creating new one...");
            }

            // Create admin user
            User adminUser = new User();
            adminUser.setUuid(java.util.UUID.randomUUID().toString());
            adminUser.setEmail("admin@wellcare.com");
            adminUser.setFirstName("WellCare");
            adminUser.setLastName("Admin");
            adminUser.setPassword("Admin123");
            adminUser.setRole("ROLE_ADMIN");
            adminUser.setLicenseNumber("ADMIN-001"); // Required for professionals
            adminUser.setActive(true);
            adminUser.setVerifiedByAdmin(true);
            adminUser.setEmailVerified(true);
            adminUser.setCreatedAt(LocalDateTime.now());
            adminUser.setUpdatedAt(LocalDateTime.now());

            // Register the admin
            User createdUser = userService.registerProfessional(adminUser, "Admin123");
            System.out.println("✅ Admin user created successfully!");
            System.out.println("Email: admin@wellcare.com");
            System.out.println("Password: Admin123");
            System.out.println("UUID: " + createdUser.getUuid());

        } catch (Exception e) {
            System.err.println("❌ Error creating admin user: " + e.getMessage());
            e.printStackTrace();
        }
    }
}