import com.wellcare.javafx.util.MyDataBase;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class InspectCheck {
    public static void main(String[] args) {
        try {
            Connection connection = MyDataBase.getInstance().getConnection();
            Statement stmt = connection.createStatement();
            
            ResultSet rs = stmt.executeQuery("SELECT CHECK_CLAUSE FROM information_schema.CHECK_CONSTRAINTS WHERE CONSTRAINT_NAME = 'backup_codes' AND CONSTRAINT_SCHEMA = 'wellora'");
            if (rs.next()) {
                System.out.println("Constraint check: " + rs.getString("CHECK_CLAUSE"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
