import com.wellcare.javafx.util.MyDataBase;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class FixDatabase {
    public static void main(String[] args) {
        try {
            Connection conn = MyDataBase.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement("UPDATE users SET diploma_url = 'dummy_diploma.pdf' WHERE role IN ('ROLE_MEDECIN', 'ROLE_COACH', 'ROLE_NUTRITIONIST')");
            int updated = stmt.executeUpdate();
            System.out.println("SUCCESS: Patched " + updated + " professionals in Database.");
        } catch(Exception e) { 
            e.printStackTrace(); 
        }
    }
}
