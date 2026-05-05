import com.wellcare.javafx.dao.UserDAO;
import com.wellcare.javafx.model.User;
import at.favre.lib.crypto.bcrypt.BCrypt;

public class ResetAdminPassword {
    public static void main(String[] args) {
        try {
            UserDAO userDAO = new UserDAO();

            // Reset password for admin@wellcare.tn to "admin123"
            try {
                User admin1 = userDAO.getUserByEmail("admin@wellcare.tn");
                if (admin1 != null) {
                    String newHash = BCrypt.hashpw("admin123", BCrypt.gensalt(12));
                    userDAO.updatePassword(admin1.getUuid(), newHash);
                    System.out.println("✅ Reset password for admin@wellcare.tn to 'admin123'");
                } else {
                    System.out.println("❌ Admin admin@wellcare.tn not found");
                }
            } catch (Exception e) {
                System.out.println("❌ Failed to reset admin@wellcare.tn: " + e.getMessage());
            }

            // Reset password for admin123@gmail.com to "admin123"
            try {
                User admin2 = userDAO.getUserByEmail("admin123@gmail.com");
                if (admin2 != null) {
                    String newHash = BCrypt.withDefaults().hashToString(12, "admin123".toCharArray());
                    userDAO.updatePassword(admin2.getUuid(), newHash);
                    System.out.println("✅ Reset password for admin123@gmail.com to 'admin123'");
                } else {
                    System.out.println("❌ Admin admin123@gmail.com not found");
                }
            } catch (Exception e) {
                System.out.println("❌ Failed to reset admin123@gmail.com: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("❌ Error resetting admin passwords: " + e.getMessage());
            e.printStackTrace();
        }
    }
}